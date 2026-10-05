package com.example.Finance_Tracker.Settings.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private static final Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    /** Upper bound for one extracted entry: a small upload can decompress to gigabytes (zip bomb). */
    public static final long MAX_ENTRY_BYTES = 10L * 1024 * 1024;

    public static byte[] createZipFromFiles(Map<String, byte[]> files) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                String filename = entry.getKey();
                byte[] data = entry.getValue();

                ZipEntry zipEntry = new ZipEntry(filename);
                zos.putNextEntry(zipEntry);
                zos.write(data);
                zos.closeEntry();

                logger.debug("Added {} to ZIP", filename);
            }

            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Error creating ZIP", e);
            throw new RuntimeException("ZIP creation failed", e);
        }
    }

    public static Map<String, InputStream> extractCsvFiles(MultipartFile zipFile, List<String> expectedFilenames) {
        Map<String, InputStream> fileMap = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (expectedFilenames.contains(entry.getName())) {
                    fileMap.put(entry.getName(), new ByteArrayInputStream(readBounded(zis, entry.getName())));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to unzip import file", e);
        }

        for (String required : expectedFilenames) {
            if (!fileMap.containsKey(required)) {
                throw new IllegalArgumentException("Missing required file in import: " + required);
            }
        }

        return fileMap;
    }

    private static byte[] readBounded(InputStream in, String name) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > MAX_ENTRY_BYTES) {
                throw new IllegalArgumentException("Import file entry is too large: " + name);
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
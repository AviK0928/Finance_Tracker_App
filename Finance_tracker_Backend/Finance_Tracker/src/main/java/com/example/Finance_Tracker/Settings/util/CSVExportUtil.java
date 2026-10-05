package com.example.Finance_Tracker.Settings.util;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Settings.entity.UserSetting;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CSVExportUtil {

    public static byte[] exportBudgetsToCSV(List<Budget> budgets) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                     CSVFormat.DEFAULT.withHeader(
                             "id", "userId", "name", "amount", "category", "startDate", "endDate",
                             "createdAt", "updatedAt", "frequency", "status", "notes",
                             "lastNotifiedStage", "expiryNotificationSent", "nearingExpiryNotificationSent", "contentHash"))) {

            for (Budget b : budgets) {
                printer.printRecord(
                        b.getId(),
                        b.getUserId(),
                        b.getName(),
                        b.getAmount(),
                        b.getCategory(),
                        b.getStartDate(),
                        b.getEndDate(),
                        b.getCreatedAt(),
                        b.getUpdatedAt(),
                        b.getFrequency(),
                        b.getStatus(),
                        b.getNotes(),
                        b.getLastNotifiedStage(),
                        b.isExpiryNotificationSent(),
                        b.isNearingExpiryNotificationSent(),
                        b.getContentHash()
                );
            }
            printer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export budgets CSV", e);
        }
    }

    public static byte[] exportTransactionsToCSV(List<Transaction> txns) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                     CSVFormat.DEFAULT.withHeader(
                             "id", "userId", "amount", "type", "category", "description",
                             "transactionDate", "createdAt", "updatedAt", "contentHash"))) {

            for (Transaction t : txns) {
                printer.printRecord(
                        t.getId(),
                        t.getUserId(),
                        t.getAmount(),
                        t.getType(),
                        t.getCategory(),
                        t.getDescription(),
                        t.getTransactionDate(),
                        t.getCreatedAt(),
                        t.getUpdatedAt(),
                        t.getContentHash()
                );
            }
            printer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export transactions CSV", e);
        }
    }

    public static byte[] exportSettingsToCSV(List<UserSetting> settings) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                     CSVFormat.DEFAULT.withHeader("id", "userId", "key", "value", "updatedAt"))) {

            for (UserSetting s : settings) {
                printer.printRecord(
                        s.getId(),
                        s.getUserId(),
                        s.getKey(),
                        s.getValue(),
                        s.getUpdatedAt()
                );
            }
            printer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export settings CSV", e);
        }
    }
}
package com.example.Finance_Tracker.Settings.util;


import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public class PDFExportUtil {

    private static final Logger logger = LoggerFactory.getLogger(PDFExportUtil.class);

    public static byte[] generateTransactionPDF(List<Transaction> transactions) {
        Document doc = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Transaction Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Header row
            Stream.of("Date", "Category", "Type", "Amount", "Description").forEach(header -> {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                headerCell.setBackgroundColor(Color.LIGHT_GRAY);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            });

            BigDecimal total = BigDecimal.ZERO;

            for (Transaction txn : transactions) {
                String dateStr = txn.getTransactionDate() != null ? txn.getTransactionDate().format(formatter) : "-";
                table.addCell(dateStr);
                table.addCell(txn.getCategory() != null ? txn.getCategory() : "-");
                table.addCell(txn.getType() != null ? capitalize(txn.getType().name()) : "-");
                table.addCell(txn.getAmount() != null ? txn.getAmount().toString() : "0.00");
                table.addCell(txn.getDescription() != null ? txn.getDescription() : "-");

                if (txn.getAmount() != null) {
                    total = total.add(BigDecimal.valueOf(txn.getAmount()));
                }
            }

            // Total row
            PdfPCell totalLabel = new PdfPCell(new Phrase("Total", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
            totalLabel.setColspan(3);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabel.setBackgroundColor(Color.GRAY);
            table.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(total.setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValue.setBackgroundColor(Color.GRAY);
            table.addCell(totalValue);

            PdfPCell filler = new PdfPCell();
            filler.setBackgroundColor(Color.GRAY);
            table.addCell(filler);

            doc.add(table);
            doc.close();

            logger.info("PDF generated with {} transactions", transactions.size());
        } catch (Exception e) {
            logger.error("PDF generation failed", e);
            throw new RuntimeException("Error generating PDF", e);
        }

        return out.toByteArray();
    }

    private static String capitalize(String raw) {
        return raw.substring(0, 1).toUpperCase() + raw.substring(1).toLowerCase();
    }
}
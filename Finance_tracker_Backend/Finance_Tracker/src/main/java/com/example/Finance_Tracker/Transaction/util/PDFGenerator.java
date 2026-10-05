package com.example.Finance_Tracker.Transaction.util;

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

public class PDFGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PDFGenerator.class);

    public static byte[] generateTransactionPDF(List<Transaction> transactions){
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
            table.setSpacingAfter(10f);

            // Table header
            Stream.of("Date", "Category", "Type", "Amount", "Description").forEach(header -> {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                headerCell.setBackgroundColor(Color.LIGHT_GRAY);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            });

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Transaction txn : transactions) {
                String dateStr = txn.getTransactionDate() != null ? txn.getTransactionDate().format(formatter) : "-";
                table.addCell(dateStr);
                table.addCell(txn.getCategory() != null ? txn.getCategory() : "-");
                table.addCell(txn.getType() != null ? txn.getType().toString() : "-");
                table.addCell(txn.getAmount() != null ? txn.getAmount().toPlainString() : "0.00");
                table.addCell(txn.getDescription() != null ? txn.getDescription() : "-");

                if (txn.getAmount() != null) {
                    totalAmount = totalAmount.add(txn.getAmount());
                }
            }

            // Total row
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
            totalLabelCell.setColspan(3);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabelCell.setBackgroundColor(Color.GRAY);
            table.addCell(totalLabelCell);

            PdfPCell totalAmountCell = new PdfPCell(new Phrase(totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
            totalAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalAmountCell.setBackgroundColor(Color.GRAY);
            table.addCell(totalAmountCell);

            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBackgroundColor(Color.GRAY);
            table.addCell(emptyCell);

            doc.add(table);
            doc.close();

            logger.info("PDF generated successfully with {} transactions.", transactions.size());
        } catch (Exception e) {
            logger.error("Error while generating PDF", e);
            throw new RuntimeException("Error while generating PDF", e);
        }

        return out.toByteArray();
    }
}

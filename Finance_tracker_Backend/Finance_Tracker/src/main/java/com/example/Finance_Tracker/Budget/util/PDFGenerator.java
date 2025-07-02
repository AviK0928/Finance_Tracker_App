package com.example.Finance_Tracker.Budget.util;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

public class PDFGenerator {

    public static byte[] generateBudgetPDF(List<Budget> budgets) {
        Document doc = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Budget Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Stream.of("Name", "Start Date", "End Date", "Amount", "Spent", "Remaining", "Status")
                    .forEach(header -> {
                        PdfPCell cell = new PdfPCell(new Phrase(header));
                        cell.setBackgroundColor(Color.LIGHT_GRAY);
                        table.addCell(cell);
                    });

            for (Budget b : budgets) {
                table.addCell(b.getName());
                table.addCell(b.getStartDate().toString());
                table.addCell(b.getEndDate().toString());
                table.addCell(String.format("%.2f", b.getAmount()));
                table.addCell(String.format("%.2f", b.getSpentAmount()));
                table.addCell(String.format("%.2f", b.getRemainingAmount()));
                table.addCell(b.getStatus().toString());
            }

            doc.add(table);
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Error while generating budget PDF", e);
        }

        return out.toByteArray();
    }
}

package com.example.Finance_Tracker.Transaction;

import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.util.PDFGenerator;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The transaction PDF totals income and expense separately (they used to be added together). */
class PDFGeneratorTest {

    private static final List<Transaction> MIXED = List.of(
            txn(TransactionType.INCOME, "1000.00"),
            txn(TransactionType.EXPENSE, "250.50"),
            txn(TransactionType.EXPENSE, "100.00"));

    @Test
    void totalsOf_keepsIncomeAndExpenseApart() {
        PDFGenerator.Totals totals = PDFGenerator.totalsOf(MIXED);

        assertThat(totals.income()).isEqualByComparingTo("1000.00");
        assertThat(totals.expense()).isEqualByComparingTo("350.50");
        assertThat(totals.net()).isEqualByComparingTo("649.50");
    }

    @Test
    void transactionPdf_showsIncomeExpenseAndNetRows() throws Exception {
        byte[] pdf = PDFGenerator.generateTransactionPDF(MIXED);

        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        String text = new PdfTextExtractor(new PdfReader(pdf)).getTextFromPage(1);
        assertThat(text).contains("Total income", "1000.00", "Total expense", "350.50", "Net", "649.50");
        assertThat(text).doesNotContain("1350.50"); // the old mixed "Total"
    }

    private static Transaction txn(TransactionType type, String amount) {
        Transaction t = new Transaction();
        t.setType(type);
        t.setAmount(new BigDecimal(amount));
        t.setCategory("Food");
        t.setTransactionDate(LocalDateTime.of(2026, 10, 5, 10, 0));
        return t;
    }
}

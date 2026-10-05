package com.example.Finance_Tracker.Transaction.entity;

import com.example.Finance_Tracker.Transaction.util.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Same package as the entity so the protected @PrePersist callback can be invoked directly. */
class TransactionHashTest {

    private static Transaction transactionWithAmount(String amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(1L);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory("Rent");
        transaction.setDescription("October rent");
        transaction.setTransactionDate(LocalDateTime.of(2026, 10, 1, 10, 0));
        transaction.onCreate();
        return transaction;
    }

    @Test
    void contentHash_ignoresAmountScale() {
        // The API may receive 12000 while the database returns 12000.00; both are the same amount.
        assertThat(transactionWithAmount("12000").getContentHash())
                .isEqualTo(transactionWithAmount("12000.00").getContentHash());
    }

    @Test
    void contentHash_changesWhenAmountChanges() {
        assertThat(transactionWithAmount("12000.00").getContentHash())
                .isNotEqualTo(transactionWithAmount("12000.01").getContentHash());
    }
}

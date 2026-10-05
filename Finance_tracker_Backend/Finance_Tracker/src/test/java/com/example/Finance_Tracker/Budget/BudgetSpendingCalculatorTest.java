package com.example.Finance_Tracker.Budget;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.service.BudgetSpendingCalculator;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real JPQL SUM queries against the dev Postgres (Flyway-migrated schema).
 * Each test runs in a transaction that is rolled back, so no data is left behind.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BudgetSpendingCalculator.class)
class BudgetSpendingCalculatorTest {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BudgetSpendingCalculator calculator;

    private Long userId;

    @BeforeEach
    void createUserWithOctoberTransactions() {
        User user = User.builder()
                .email("spending-" + System.nanoTime() + "@example.com")
                .username("spending")
                .password("not-a-real-hash")
                .build();
        userId = userRepository.save(user).getId();

        // Inside October 2026
        save(TransactionType.EXPENSE, "Food", "0.10", LocalDateTime.of(2026, 10, 1, 0, 0));       // first instant
        save(TransactionType.EXPENSE, "food", "0.20", LocalDateTime.of(2026, 10, 15, 12, 0));     // different case
        save(TransactionType.EXPENSE, "Food", "5.00", LocalDateTime.of(2026, 10, 31, 23, 59, 59)); // last second
        save(TransactionType.EXPENSE, "Rent", "1000.00", LocalDateTime.of(2026, 10, 5, 9, 0));
        save(TransactionType.INCOME, "Food", "50000.00", LocalDateTime.of(2026, 10, 1, 9, 0));   // income never counts

        // Outside October 2026
        save(TransactionType.EXPENSE, "Food", "7.00", LocalDateTime.of(2026, 9, 30, 23, 59, 59));
        save(TransactionType.EXPENSE, "Food", "100.00", LocalDateTime.of(2026, 11, 1, 0, 0));
    }

    private void save(TransactionType type, String category, String amount, LocalDateTime date) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setTransactionDate(date);
        transactionRepository.save(transaction);
    }

    private Budget octoberBudget(String category) {
        return Budget.builder()
                .userId(userId)
                .name("October")
                .category(category)
                .amount(new BigDecimal("2000.00"))
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2026, 10, 31))
                .build();
    }

    @Test
    void categoryBudget_sumsOnlyThatCategoryWithinDates_caseInsensitive() {
        assertThat(calculator.spentFor(octoberBudget("FOOD"))).isEqualByComparingTo("5.30");
    }

    @Test
    void budgetWithoutCategory_sumsAllExpensesWithinDates() {
        assertThat(calculator.spentFor(octoberBudget(null))).isEqualByComparingTo("1005.30");
    }

    @Test
    void budgetWithNoMatchingTransactions_isZero() {
        assertThat(calculator.spentFor(octoberBudget("Travel"))).isEqualByComparingTo("0");
    }
}

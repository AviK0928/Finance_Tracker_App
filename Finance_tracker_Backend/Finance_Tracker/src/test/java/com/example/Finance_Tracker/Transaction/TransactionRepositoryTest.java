package com.example.Finance_Tracker.Transaction;

import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Dashboard queries against the dev Postgres; each test is rolled back. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void sumAmountByType_totalsEachTypeForThatUserOnly() {
        Long userId = newUser();
        Long otherUserId = newUser();
        save(userId, TransactionType.INCOME, "1000.00");
        save(userId, TransactionType.EXPENSE, "30.25");
        save(userId, TransactionType.EXPENSE, "19.75");
        save(otherUserId, TransactionType.EXPENSE, "999.00");

        Map<TransactionType, BigDecimal> totals = transactionRepository.sumAmountByType(userId).stream()
                .collect(Collectors.toMap(TransactionRepository.TypeTotal::getTransactionType,
                        TransactionRepository.TypeTotal::getTotal));

        assertThat(totals).hasSize(2);
        assertThat(totals.get(TransactionType.INCOME)).isEqualByComparingTo("1000.00");
        assertThat(totals.get(TransactionType.EXPENSE)).isEqualByComparingTo("50.00");
    }

    @Test
    void findByUserIdAndUpdatedAtAfter_returnsOnlyThatUsersRowsChangedInTheWindow() {
        Long userId = newUser();
        Long otherUserId = newUser();
        LocalDateTime before = LocalDateTime.now().minusMinutes(1);
        save(userId, TransactionType.EXPENSE, "10.00");
        save(otherUserId, TransactionType.EXPENSE, "20.00");

        assertThat(transactionRepository.findByUserIdAndUpdatedAtAfter(userId, before))
                .extracting(Transaction::getUserId).containsExactly(userId);
        assertThat(transactionRepository.findByUserIdAndUpdatedAtAfter(userId, LocalDateTime.now().plusMinutes(1)))
                .isEmpty();
    }

    private Long newUser() {
        return userRepository.save(User.builder()
                .email("tx-repo-" + System.nanoTime() + "@example.com")
                .username("tx-repo")
                .password("not-a-real-hash")
                .build()).getId();
    }

    private void save(Long owner, TransactionType type, String amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(owner);
        transaction.setType(type);
        transaction.setCategory("Food");
        transaction.setAmount(new BigDecimal(amount));
        transaction.setTransactionDate(LocalDateTime.of(2026, 10, 5, 12, 0));
        transactionRepository.save(transaction);
    }
}

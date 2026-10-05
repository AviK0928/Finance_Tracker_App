package com.example.Finance_Tracker.Transaction;

import com.example.Finance_Tracker.Transaction.entity.DeletedTransaction;
import com.example.Finance_Tracker.Transaction.repository.DeletedTransactionRepository;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Deletion records used by the delta sync, against the dev Postgres (V4 table); each test is rolled back. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeletedTransactionRepositoryTest {

    @Autowired private DeletedTransactionRepository deletedTransactionRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findTransactionIdsDeletedSince_returnsThatUsersIdsAfterTheGivenTime() {
        Long userId = newUser();
        Long otherUserId = newUser();
        LocalDateTime since = LocalDateTime.of(2026, 10, 5, 12, 0);
        deletedTransactionRepository.save(new DeletedTransaction(userId, 1L, since.minusMinutes(5)));
        deletedTransactionRepository.save(new DeletedTransaction(userId, 2L, since.plusMinutes(5)));
        deletedTransactionRepository.save(new DeletedTransaction(otherUserId, 3L, since.plusMinutes(5)));

        assertThat(deletedTransactionRepository.findTransactionIdsDeletedSince(userId, since)).containsExactly(2L);
    }

    private Long newUser() {
        return userRepository.save(User.builder()
                .email("deleted-tx-" + System.nanoTime() + "@example.com")
                .username("deleted-tx")
                .password("not-a-real-hash")
                .build()).getId();
    }
}

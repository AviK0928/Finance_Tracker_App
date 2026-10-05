package com.example.Finance_Tracker.Budget;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Real Postgres, rolled back after each test. Assertions only look at this test's own users. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BudgetRepositoryTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_31 = LocalDate.of(2026, 10, 31);

    @Autowired private BudgetRepository budgetRepository;
    @Autowired private UserRepository userRepository;

    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void createUsers() {
        userId = newUser();
        otherUserId = newUser();
    }

    private Long newUser() {
        return userRepository.save(User.builder()
                .email("budget-repo-" + System.nanoTime() + "@example.com")
                .username("budget-repo")
                .password("not-a-real-hash")
                .build()).getId();
    }

    private Budget save(Long owner, String name, String category, BudgetStatus status, LocalDate start, LocalDate end) {
        return budgetRepository.save(Budget.builder()
                .userId(owner)
                .name(name)
                .category(category)
                .amount(new BigDecimal("1000.00"))
                .startDate(start)
                .endDate(end)
                .frequency(BudgetFrequency.MONTHLY)
                .status(status)
                .build());
    }

    @Test
    void findBudgetsCovering_returnsActiveBudgetsForThatCategoryOrAllCategories_containingTheDate() {
        save(userId, "Food Oct", "Food", BudgetStatus.ACTIVE, OCT_1, OCT_31);
        save(userId, "All Oct", null, BudgetStatus.ACTIVE, OCT_1, OCT_31);
        save(userId, "Rent Oct", "Rent", BudgetStatus.ACTIVE, OCT_1, OCT_31);                 // other category
        save(userId, "Food Oct paused", "Food", BudgetStatus.PAUSED, OCT_1, OCT_31);          // not active
        save(userId, "Food Nov", "Food", BudgetStatus.ACTIVE, OCT_31.plusDays(1), OCT_31.plusDays(30)); // other dates
        save(otherUserId, "Their Food", "Food", BudgetStatus.ACTIVE, OCT_1, OCT_31);          // other user

        List<String> names = budgetRepository
                .findBudgetsCovering(userId, BudgetStatus.ACTIVE, OCT_31, "FOOD").stream()
                .map(Budget::getName)
                .toList();

        assertThat(names).containsExactlyInAnyOrder("Food Oct", "All Oct");
    }

    @Test
    void expiredQuery_returnsOnlyActiveEndedBudgetsNotYetNotified() {
        save(userId, "September", "Food", BudgetStatus.ACTIVE, OCT_1.minusMonths(1), OCT_1.minusDays(1));
        Budget alreadyNotified = save(userId, "August", "Food", BudgetStatus.ACTIVE,
                OCT_1.minusMonths(2), OCT_1.minusMonths(1).minusDays(1));
        alreadyNotified.setExpiryNotificationSent(true); // @PrePersist resets it on insert, so set it via update
        budgetRepository.save(alreadyNotified);
        save(userId, "September cancelled", "Food", BudgetStatus.CANCELLED, OCT_1.minusMonths(1), OCT_1.minusDays(1));
        save(userId, "October", "Food", BudgetStatus.ACTIVE, OCT_1, OCT_31);                  // not ended yet

        List<String> names = budgetRepository
                .findByStatusAndExpiryNotificationSentFalseAndEndDateBefore(BudgetStatus.ACTIVE, OCT_1).stream()
                .filter(b -> b.getUserId().equals(userId))
                .map(Budget::getName)
                .toList();

        assertThat(names).containsExactly("September");
    }
}

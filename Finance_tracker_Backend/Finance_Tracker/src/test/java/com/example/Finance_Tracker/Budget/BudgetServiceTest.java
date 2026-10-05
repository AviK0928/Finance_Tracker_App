package com.example.Finance_Tracker.Budget;

import com.example.Finance_Tracker.Budget.dto.BudgetUpdateDTO;
import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.service.BudgetAlertService;
import com.example.Finance_Tracker.Budget.service.BudgetService;
import com.example.Finance_Tracker.Budget.service.BudgetSpendingCalculator;
import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Budget.util.BudgetUsageAlertStage;
import com.example.Finance_Tracker.User.mapper.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Updating a budget keeps its alert state consistent with the new values. */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    private static final long USER_ID = 7L;
    private static final LocalDate OCT_31 = LocalDate.of(2026, 10, 31);

    @Mock private BudgetRepository budgetRepository;
    @Mock private BudgetAlertService budgetAlertService;
    @Mock private BudgetSpendingCalculator spendingCalculator;
    @InjectMocks private BudgetService budgetService;

    @BeforeEach
    void login() {
        CustomUserDetails principal = new CustomUserDetails(USER_ID, "me@example.com", "hash", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extendingTheEndDate_reArmsTheExpiryReminders() {
        Budget budget = expiredAndAnnounced();
        stubUpdate(budget);

        budgetService.updateBudget(1L, update(OCT_31.plusMonths(1)));

        assertThat(budget.isExpiryNotificationSent()).isFalse();
        assertThat(budget.isNearingExpiryNotificationSent()).isFalse();
        verify(budgetAlertService).evaluate(budget);
    }

    @Test
    void keepingTheEndDate_keepsTheExpiryRemindersSent() {
        Budget budget = expiredAndAnnounced();
        stubUpdate(budget);

        budgetService.updateBudget(1L, update(OCT_31));

        assertThat(budget.isExpiryNotificationSent()).isTrue();
        assertThat(budget.isNearingExpiryNotificationSent()).isTrue();
    }

    private void stubUpdate(Budget budget) {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(spendingCalculator.spentFor(budget)).thenReturn(BigDecimal.ZERO);
    }

    private static Budget expiredAndAnnounced() {
        return Budget.builder()
                .id(1L).userId(USER_ID).name("Groceries").amount(new BigDecimal("1000.00"))
                .startDate(LocalDate.of(2026, 10, 1)).endDate(OCT_31)
                .frequency(BudgetFrequency.MONTHLY).status(BudgetStatus.ACTIVE)
                .lastNotifiedStage(BudgetUsageAlertStage.NONE)
                .expiryNotificationSent(true).nearingExpiryNotificationSent(true)
                .build();
    }

    private static BudgetUpdateDTO update(LocalDate endDate) {
        return BudgetUpdateDTO.builder()
                .name("Groceries").amount(new BigDecimal("1000.00"))
                .startDate(LocalDate.of(2026, 10, 1)).endDate(endDate)
                .frequency(BudgetFrequency.MONTHLY).status(BudgetStatus.ACTIVE)
                .build();
    }
}

package com.example.Finance_Tracker.Budget.scheduler;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetNotificationSchedulerTest {

    private static final long OWNER_ID = 42L;

    @Mock private BudgetRepository budgetRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private BudgetNotificationScheduler scheduler;

    private static Budget budgetEndingOn(LocalDate endDate) {
        return Budget.builder()
                .id(3L)
                .userId(OWNER_ID)
                .name("Groceries")
                .amount(new BigDecimal("1000.00"))
                .startDate(endDate.minusDays(30))
                .endDate(endDate)
                .status(BudgetStatus.ACTIVE)
                .build();
    }

    private CreateNotificationDTO notificationSentToOwner() {
        ArgumentCaptor<CreateNotificationDTO> captor = ArgumentCaptor.forClass(CreateNotificationDTO.class);
        verify(notificationService).createNotificationForUser(eq(OWNER_ID), captor.capture());
        // The old code path required a logged-in user and threw on the scheduler thread
        verify(notificationService, never()).createNotification(any());
        return captor.getValue();
    }

    @Test
    void expiredBudget_ownerIsNotifiedAndBudgetFlagged() {
        LocalDate today = LocalDate.now();
        Budget budget = budgetEndingOn(today.minusDays(1));
        when(budgetRepository.findByStatusAndExpiryNotificationSentFalseAndEndDateBefore(BudgetStatus.ACTIVE, today))
                .thenReturn(List.of(budget));

        scheduler.notifyExpiredBudgets();

        assertThat(notificationSentToOwner().getTitle()).isEqualTo("Budget Expired");
        assertThat(budget.isExpiryNotificationSent()).isTrue();
        verify(budgetRepository).save(budget);
    }

    @Test
    void budgetEndingInThreeDays_ownerIsNotifiedAndBudgetFlagged() {
        LocalDate inThreeDays = LocalDate.now().plusDays(3);
        Budget budget = budgetEndingOn(inThreeDays);
        when(budgetRepository.findByStatusAndNearingExpiryNotificationSentFalseAndEndDate(BudgetStatus.ACTIVE, inThreeDays))
                .thenReturn(List.of(budget));

        scheduler.notifyNearingExpiryBudgets();

        assertThat(notificationSentToOwner().getTitle()).isEqualTo("Budget Nearing Expiry");
        assertThat(budget.isNearingExpiryNotificationSent()).isTrue();
        verify(budgetRepository).save(budget);
    }
}

package com.example.Finance_Tracker.Budget.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Budget.util.BudgetUsageAlertStage;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetAlertServiceTest {

    private static final long USER_ID = 7L;
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @Mock private BudgetRepository budgetRepository;
    @Mock private BudgetSpendingCalculator spendingCalculator;
    @Mock private NotificationService notificationService;

    @InjectMocks private BudgetAlertService alertService;

    private static Budget budget(long id, BudgetUsageAlertStage lastNotified) {
        return Budget.builder()
                .id(id)
                .userId(USER_ID)
                .name("Budget " + id)
                .amount(new BigDecimal("1000.00"))
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2026, 10, 31))
                .status(BudgetStatus.ACTIVE)
                .lastNotifiedStage(lastNotified)
                .build();
    }

    private void spent(Budget budget, String amount) {
        when(spendingCalculator.spentFor(budget)).thenReturn(new BigDecimal(amount));
    }

    private String onlyNotificationTitle() {
        ArgumentCaptor<CreateNotificationDTO> captor = ArgumentCaptor.forClass(CreateNotificationDTO.class);
        verify(notificationService, times(1)).createNotificationForUser(eq(USER_ID), captor.capture());
        return captor.getValue().getTitle();
    }

    @ParameterizedTest(name = "spent {0} of 1000.00 -> {1}")
    @CsvSource({
            "0, NONE",
            "499.99, NONE",
            "500.00, FIFTY_PERCENT",
            "899.99, FIFTY_PERCENT",
            "900.00, NINETY_PERCENT",
            "1000.00, NINETY_PERCENT",
            "1000.01, EXCEEDED"
    })
    void stageFor_thresholdBoundaries(String spent, BudgetUsageAlertStage expected) {
        assertThat(BudgetAlertService.stageFor(new BigDecimal(spent), new BigDecimal("1000.00"))).isEqualTo(expected);
    }

    @Test
    void belowFiftyPercent_sendsNothing() {
        Budget budget = budget(1L, BudgetUsageAlertStage.NONE);
        spent(budget, "499.99");

        alertService.evaluate(budget);

        verifyNoInteractions(notificationService);
        verify(budgetRepository, never()).save(any());
        assertThat(budget.getLastNotifiedStage()).isEqualTo(BudgetUsageAlertStage.NONE);
    }

    @Test
    void jumpFromZeroToOverBudget_sendsOnlyTheExceededNotification() {
        Budget budget = budget(1L, BudgetUsageAlertStage.NONE);
        spent(budget, "1200.00");

        alertService.evaluate(budget);

        assertThat(onlyNotificationTitle()).isEqualTo("Budget Exceeded");
        assertThat(budget.getLastNotifiedStage()).isEqualTo(BudgetUsageAlertStage.EXCEEDED);
        verify(budgetRepository).save(budget);
    }

    @Test
    void crossingNinetyAfterFiftyWasNotified_sendsNinety() {
        Budget budget = budget(1L, BudgetUsageAlertStage.FIFTY_PERCENT);
        spent(budget, "900.00");

        alertService.evaluate(budget);

        assertThat(onlyNotificationTitle()).isEqualTo("90% Budget Used");
        assertThat(budget.getLastNotifiedStage()).isEqualTo(BudgetUsageAlertStage.NINETY_PERCENT);
    }

    @Test
    void stageAlreadyNotified_isNotRepeated() {
        Budget budget = budget(1L, BudgetUsageAlertStage.NINETY_PERCENT);
        spent(budget, "950.00");

        alertService.evaluate(budget);

        verifyNoInteractions(notificationService);
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void onExpenseRecorded_evaluatesEveryBudgetTheExpenseCountsTowards() {
        Budget food = budget(1L, BudgetUsageAlertStage.NONE);
        Budget overall = budget(2L, BudgetUsageAlertStage.NONE);
        when(budgetRepository.findBudgetsCovering(USER_ID, BudgetStatus.ACTIVE, OCT_15, "Food"))
                .thenReturn(List.of(food, overall));
        spent(food, "600.00");    // crosses 50%
        spent(overall, "100.00"); // below 50%

        alertService.onExpenseRecorded(USER_ID, "Food", OCT_15);

        assertThat(onlyNotificationTitle()).isEqualTo("50% Budget Used");
        assertThat(food.getLastNotifiedStage()).isEqualTo(BudgetUsageAlertStage.FIFTY_PERCENT);
        assertThat(overall.getLastNotifiedStage()).isEqualTo(BudgetUsageAlertStage.NONE);
    }
}

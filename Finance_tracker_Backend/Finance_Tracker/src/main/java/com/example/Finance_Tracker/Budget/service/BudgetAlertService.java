package com.example.Finance_Tracker.Budget.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Budget.util.BudgetUsageAlertStage;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Budget usage alerts (50% / 90% / exceeded).
 * Each budget remembers the highest stage already notified ({@code lastNotifiedStage}),
 * so every stage is announced at most once, and a single large expense that jumps several
 * stages produces one notification for the highest stage reached, not one per stage.
 */
@Service
public class BudgetAlertService {

    private static final BigDecimal FIFTY_PERCENT = new BigDecimal("0.50");
    private static final BigDecimal NINETY_PERCENT = new BigDecimal("0.90");

    private final BudgetRepository budgetRepository;
    private final BudgetSpendingCalculator spendingCalculator;
    private final NotificationService notificationService;

    public BudgetAlertService(BudgetRepository budgetRepository,
                              BudgetSpendingCalculator spendingCalculator,
                              NotificationService notificationService) {
        this.budgetRepository = budgetRepository;
        this.spendingCalculator = spendingCalculator;
        this.notificationService = notificationService;
    }

    /** Re-checks every active budget an expense on {@code date} in {@code category} counts towards. */
    public void onExpenseRecorded(Long userId, String category, LocalDate date) {
        budgetRepository.findBudgetsCovering(userId, BudgetStatus.ACTIVE, date, category)
                .forEach(this::evaluate);
    }

    /** Notifies the owner if the budget reached a stage higher than the last one notified. */
    public void evaluate(Budget budget) {
        BigDecimal spent = spendingCalculator.spentFor(budget);
        BudgetUsageAlertStage reached = stageFor(spent, budget.getAmount());
        if (reached.ordinal() <= budget.getLastNotifiedStage().ordinal()) {
            return;
        }
        notificationService.createNotificationForUser(budget.getUserId(), notificationFor(budget, reached));
        budget.setLastNotifiedStage(reached);
        budgetRepository.save(budget);
    }

    static BudgetUsageAlertStage stageFor(BigDecimal spent, BigDecimal budgetAmount) {
        if (budgetAmount == null || budgetAmount.signum() <= 0) {
            return BudgetUsageAlertStage.NONE;
        }
        if (spent.compareTo(budgetAmount) > 0) {
            return BudgetUsageAlertStage.EXCEEDED;
        }
        if (spent.compareTo(budgetAmount.multiply(NINETY_PERCENT)) >= 0) {
            return BudgetUsageAlertStage.NINETY_PERCENT;
        }
        if (spent.compareTo(budgetAmount.multiply(FIFTY_PERCENT)) >= 0) {
            return BudgetUsageAlertStage.FIFTY_PERCENT;
        }
        return BudgetUsageAlertStage.NONE;
    }

    private static CreateNotificationDTO notificationFor(Budget budget, BudgetUsageAlertStage stage) {
        CreateNotificationDTO dto = new CreateNotificationDTO();
        dto.setReferenceId(budget.getId());
        dto.setPreference(SettingKey.NOTIFY_SPENDING_ALERTS);
        switch (stage) {
            case FIFTY_PERCENT -> {
                dto.setTitle("50% Budget Used");
                dto.setMessage("You've used 50% of your budget: " + budget.getName());
                dto.setType(NotificationType.INFO);
            }
            case NINETY_PERCENT -> {
                dto.setTitle("90% Budget Used");
                dto.setMessage("You're almost out of budget: " + budget.getName());
                dto.setType(NotificationType.WARNING);
            }
            case EXCEEDED -> {
                dto.setTitle("Budget Exceeded");
                dto.setMessage("You've exceeded your budget: " + budget.getName());
                dto.setType(NotificationType.ALERT);
            }
            default -> throw new IllegalArgumentException("No notification for stage " + stage);
        }
        return dto;
    }
}

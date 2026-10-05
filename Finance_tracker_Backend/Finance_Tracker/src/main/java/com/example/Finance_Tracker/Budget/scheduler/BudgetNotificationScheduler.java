package com.example.Finance_Tracker.Budget.scheduler;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily budget expiry notifications.
 * Runs without a logged-in user, so notifications are addressed explicitly with
 * {@link NotificationService#createNotificationForUser(Long, CreateNotificationDTO)}.
 * Schedules are configurable (see application.properties) so they can be shortened for testing.
 */
@Component
public class BudgetNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(BudgetNotificationScheduler.class);
    static final int NEARING_EXPIRY_DAYS = 3;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private NotificationService notificationService;

    /** Active budgets whose end date has passed and that haven't been announced as expired yet. */
    @Scheduled(cron = "${budget.notifications.expired-cron}")
    @Transactional
    public void notifyExpiredBudgets() {
        List<Budget> expired = budgetRepository
                .findByStatusAndExpiryNotificationSentFalseAndEndDateBefore(BudgetStatus.ACTIVE, LocalDate.now());

        for (Budget budget : expired) {
            notificationService.createNotificationForUser(budget.getUserId(), notification(
                    "Budget Expired",
                    "Your budget \"" + budget.getName() + "\" has expired.",
                    NotificationType.ALERT, budget));
            budget.setExpiryNotificationSent(true);
            budgetRepository.save(budget);
        }
        log.info("Budget expiry job: {} budget(s) notified", expired.size());
    }

    /** Active budgets ending in exactly NEARING_EXPIRY_DAYS days, not yet announced. */
    @Scheduled(cron = "${budget.notifications.nearing-expiry-cron}")
    @Transactional
    public void notifyNearingExpiryBudgets() {
        LocalDate endDate = LocalDate.now().plusDays(NEARING_EXPIRY_DAYS);
        List<Budget> nearing = budgetRepository
                .findByStatusAndNearingExpiryNotificationSentFalseAndEndDate(BudgetStatus.ACTIVE, endDate);

        for (Budget budget : nearing) {
            notificationService.createNotificationForUser(budget.getUserId(), notification(
                    "Budget Nearing Expiry",
                    "Your budget \"" + budget.getName() + "\" will expire in " + NEARING_EXPIRY_DAYS + " days.",
                    NotificationType.INFO, budget));
            budget.setNearingExpiryNotificationSent(true);
            budgetRepository.save(budget);
        }
        log.info("Budget nearing-expiry job: {} budget(s) notified", nearing.size());
    }

    private static CreateNotificationDTO notification(String title, String message, NotificationType type, Budget budget) {
        CreateNotificationDTO dto = new CreateNotificationDTO();
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setType(type);
        dto.setReferenceId(budget.getId());
        return dto;
    }
}

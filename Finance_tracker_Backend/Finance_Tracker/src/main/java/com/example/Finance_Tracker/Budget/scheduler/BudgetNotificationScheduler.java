package com.example.Finance_Tracker.Budget.scheduler;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BudgetNotificationScheduler {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Runs once every day at 9 AM to notify users about expired budgets
     */
    @Scheduled(cron = "0 0 9 * * *") // 9:00 AM daily
    @Transactional
    public void notifyExpiredBudgets() {
        LocalDate today = LocalDate.now();

        List<Budget> expiredBudgets = budgetRepository.findAll().stream()
                .filter(budget -> !budget.isExpiryNotificationSent())
                .filter(budget -> budget.getEndDate().isBefore(today))
                .toList();

        for (Budget budget : expiredBudgets) {
            CreateNotificationDTO dto = new CreateNotificationDTO();
            dto.setTitle("Budget Expired");
            dto.setMessage("Your budget \"" + budget.getName() + "\" has expired.");
            dto.setType(NotificationType.ALERT);
            dto.setReferenceId(budget.getId());

            notificationService.createNotification(dto);

            budget.setExpiryNotificationSent(true);
            budgetRepository.save(budget);
        }
    }

    @Scheduled(cron = "0 15 9 * * *") // 9:15 AM daily
    @Transactional
    public void notifyNearingExpiryBudgets() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(3);

        List<Budget> nearingExpiryBudgets = budgetRepository.findAll().stream()
                .filter(budget -> !budget.isNearingExpiryNotificationSent())
                .filter(budget -> budget.getEndDate().isEqual(threshold))
                .toList();

        for (Budget budget : nearingExpiryBudgets) {
            CreateNotificationDTO dto = new CreateNotificationDTO();
            dto.setTitle("Budget Nearing Expiry");
            dto.setMessage("Your budget \"" + budget.getName() + "\" will expire in 3 days.");
            dto.setType(NotificationType.INFO);
            dto.setReferenceId(budget.getId());

            notificationService.createNotification(dto);
            budget.setNearingExpiryNotificationSent(true);
            budgetRepository.save(budget);
        }
    }
}
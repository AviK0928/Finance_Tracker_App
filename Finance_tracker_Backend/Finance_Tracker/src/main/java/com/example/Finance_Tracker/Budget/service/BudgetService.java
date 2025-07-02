package com.example.Finance_Tracker.Budget.service;

import com.example.Finance_Tracker.Budget.util.BudgetUsageAlertStage;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Budget.dto.BudgetCreateDTO;
import com.example.Finance_Tracker.Budget.dto.BudgetFilterDTO;
import com.example.Finance_Tracker.Budget.dto.BudgetResponseDTO;
import com.example.Finance_Tracker.Budget.dto.BudgetUpdateDTO;
import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.exception.BudgetNotFoundException;
import com.example.Finance_Tracker.Budget.exception.UnauthorizedBudgetAccessException;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.util.BudgetSpecification;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private NotificationService notificationService;

    public BudgetResponseDTO createBudget(BudgetCreateDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Budget budget = Budget.builder()
                .userId(currentUserId) // always assign current user
                .name(dto.getName())
                .amount(dto.getAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .frequency(dto.getFrequency())
                .notes(dto.getNotes())
                .build();

        budget = budgetRepository.save(budget);
        return BudgetResponseDTO.fromEntity(budget);
    }

    public List<Budget> getFilteredBudgets(BudgetFilterDTO filter) {
        Specification<Budget> spec = new BudgetSpecification(filter);
        return budgetRepository.findAll(spec);
    }

    public BudgetResponseDTO updateBudget(Long budgetId, BudgetUpdateDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));

        if (!budget.getUserId().equals(currentUserId)) {
            throw new UnauthorizedBudgetAccessException("You do not have permission to update this budget.");
        }

        budget.setName(dto.getName());
        budget.setAmount(dto.getAmount());
        budget.setStartDate(dto.getStartDate());
        budget.setEndDate(dto.getEndDate());
        budget.setFrequency(dto.getFrequency());
        budget.setNotes(dto.getNotes());
        budget.setStatus(dto.getStatus());

        budget = budgetRepository.save(budget);

        BigDecimal spent = budget.getSpentAmount();
        BigDecimal total = budget.getAmount();

        if (spent != null && total != null) {
            BigDecimal fifty = total.multiply(BigDecimal.valueOf(0.5));
            BigDecimal ninety = total.multiply(BigDecimal.valueOf(0.9));

            if (spent.compareTo(fifty) >= 0 &&
                    budget.getLastNotifiedStage().ordinal() < BudgetUsageAlertStage.FIFTY_PERCENT.ordinal()) {

                CreateNotificationDTO dto50 = new CreateNotificationDTO();
                dto50.setTitle("50% Budget Used");
                dto50.setMessage("You've used 50% of your budget: " + budget.getName());
                dto50.setType(NotificationType.INFO);
                dto50.setReferenceId(budget.getId());

                notificationService.createNotification(dto50);
                budget.setLastNotifiedStage(BudgetUsageAlertStage.FIFTY_PERCENT);
            }

            if (spent.compareTo(ninety) >= 0 &&
                    budget.getLastNotifiedStage().ordinal() < BudgetUsageAlertStage.NINETY_PERCENT.ordinal()) {

                CreateNotificationDTO dto90 = new CreateNotificationDTO();
                dto90.setTitle("90% Budget Used");
                dto90.setMessage("You're almost out of budget: " + budget.getName());
                dto90.setType(NotificationType.WARNING);
                dto90.setReferenceId(budget.getId());

                notificationService.createNotification(dto90);
                budget.setLastNotifiedStage(BudgetUsageAlertStage.NINETY_PERCENT);
            }

            if (spent.compareTo(total) > 0 &&
                    budget.getLastNotifiedStage().ordinal() < BudgetUsageAlertStage.EXCEEDED.ordinal()) {

                CreateNotificationDTO dtoExceeded = new CreateNotificationDTO();
                dtoExceeded.setTitle("Budget Exceeded");
                dtoExceeded.setMessage("You've exceeded your budget: " + budget.getName());
                dtoExceeded.setType(NotificationType.ALERT);
                dtoExceeded.setReferenceId(budget.getId());

                notificationService.createNotification(dtoExceeded);
                budget.setLastNotifiedStage(BudgetUsageAlertStage.EXCEEDED);
            }

            budget = budgetRepository.save(budget); // persist lastNotifiedStage changes
        }

        return BudgetResponseDTO.fromEntity(budget);
    }

    public void deleteBudget(Long budgetId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));

        if (!budget.getUserId().equals(currentUserId)) {
            throw new UnauthorizedBudgetAccessException("You do not have permission to delete this budget.");
        }

        budgetRepository.delete(budget);
    }

    public BudgetResponseDTO getBudgetById(Long budgetId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException(budgetId));

        if (!budget.getUserId().equals(currentUserId)) {
            throw new UnauthorizedBudgetAccessException("You do not have permission to view this budget.");
        }

        return BudgetResponseDTO.fromEntity(budget);
    }

    public List<BudgetResponseDTO> getBudgetsByUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        List<Budget> budgets = budgetRepository.findByUserId(currentUserId);
        return budgets.stream()
                .map(BudgetResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

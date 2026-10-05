package com.example.Finance_Tracker.Budget.service;

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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetAlertService budgetAlertService;

    @Autowired
    private BudgetSpendingCalculator spendingCalculator;

    /** Trims the category; blank means "all categories" (stored as null). */
    private static String normalizeCategory(String category) {
        return (category == null || category.isBlank()) ? null : category.trim();
    }

    private BudgetResponseDTO toResponse(Budget budget) {
        return BudgetResponseDTO.fromEntity(budget, spendingCalculator.spentFor(budget));
    }

    public BudgetResponseDTO createBudget(BudgetCreateDTO dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Budget budget = Budget.builder()
                .userId(currentUserId) // always assign current user
                .name(dto.getName())
                .category(normalizeCategory(dto.getCategory()))
                .amount(dto.getAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .frequency(dto.getFrequency())
                .notes(dto.getNotes())
                .build();

        budget = budgetRepository.save(budget);
        budgetAlertService.evaluate(budget); // a new budget may already be over a threshold
        return toResponse(budget);
    }

    /** Lists: spending of all budgets in one query instead of one per budget. */
    private List<BudgetResponseDTO> toResponses(List<Budget> budgets) {
        Map<Long, BigDecimal> spent = spendingCalculator.spentForAll(budgets);
        return budgets.stream()
                .map(b -> BudgetResponseDTO.fromEntity(b, spent.getOrDefault(b.getId(), BigDecimal.ZERO)))
                .collect(Collectors.toList());
    }

    public List<BudgetResponseDTO> getFilteredBudgets(BudgetFilterDTO filter) {
        Specification<Budget> spec = new BudgetSpecification(filter);
        return toResponses(budgetRepository.findAll(spec));
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

        if (!dto.getEndDate().equals(budget.getEndDate())) {
            // New end date: the expiry reminders apply to it again
            budget.setExpiryNotificationSent(false);
            budget.setNearingExpiryNotificationSent(false);
        }
        budget.setName(dto.getName());
        budget.setCategory(normalizeCategory(dto.getCategory()));
        budget.setAmount(dto.getAmount());
        budget.setStartDate(dto.getStartDate());
        budget.setEndDate(dto.getEndDate());
        budget.setFrequency(dto.getFrequency());
        budget.setNotes(dto.getNotes());
        budget.setStatus(dto.getStatus());

        budget = budgetRepository.save(budget);
        budgetAlertService.evaluate(budget); // amount/dates/category changes can cross a threshold

        return toResponse(budget);
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

        return toResponse(budget);
    }

    public List<BudgetResponseDTO> getBudgetsByUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return toResponses(budgetRepository.findByUserId(currentUserId));
    }
}

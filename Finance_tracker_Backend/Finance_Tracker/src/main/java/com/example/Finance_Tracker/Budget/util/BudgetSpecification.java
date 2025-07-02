package com.example.Finance_Tracker.Budget.util;

import com.example.Finance_Tracker.Budget.dto.BudgetFilterDTO;
import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Security.SecurityUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class BudgetSpecification implements Specification<Budget> {
    private final BudgetFilterDTO filter;

    public BudgetSpecification(BudgetFilterDTO filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<Budget> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();

        // Get current user ID from SecurityUtils
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null) {
            predicate = cb.and(predicate, cb.equal(root.get("userId"), currentUserId));
        } else {
            // User is not authenticated, return a predicate that matches nothing
            predicate = cb.disjunction();
            return predicate;
        }

        // Apply other filters if present
        if (filter.getStatus() != null) {
            predicate = cb.and(predicate, cb.equal(root.get("status"), filter.getStatus()));
        }

        if (filter.getFrequency() != null) {
            predicate = cb.and(predicate, cb.equal(root.get("frequency"), filter.getFrequency()));
        }

        return predicate;
    }
}

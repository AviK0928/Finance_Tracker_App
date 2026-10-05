package com.example.Finance_Tracker.Budget.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes how much of a budget has been spent: the sum of the owner's EXPENSE transactions
 * dated within the budget's (inclusive) start/end dates, restricted to the budget's category if it has one.
 */
@Component
public class BudgetSpendingCalculator {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public BudgetSpendingCalculator(TransactionRepository transactionRepository,
                                    BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    public BigDecimal spentFor(Budget budget) {
        LocalDateTime from = budget.getStartDate().atStartOfDay();
        LocalDateTime toExclusive = budget.getEndDate().plusDays(1).atStartOfDay();

        BigDecimal sum = (budget.getCategory() == null)
                ? transactionRepository.sumAmount(budget.getUserId(), TransactionType.EXPENSE, from, toExclusive)
                : transactionRepository.sumAmountForCategory(budget.getUserId(), TransactionType.EXPENSE,
                        budget.getCategory(), from, toExclusive);

        return sum != null ? sum : BigDecimal.ZERO;
    }

    /**
     * Spending of several saved budgets with one query (budget lists, dashboard), keyed by budget id.
     * Read paths only: alert evaluation runs inside writes and relies on Hibernate auto-flushing
     * pending changes before its JPQL sums, which is not guaranteed the same way for a native query,
     * so that path keeps using {@link #spentFor(Budget)}.
     */
    public Map<Long, BigDecimal> spentForAll(Collection<Budget> budgets) {
        if (budgets.isEmpty()) {
            return Map.of(); // "IN ()" is not valid SQL
        }
        List<Long> ids = budgets.stream().map(Budget::getId).toList();
        Map<Long, BigDecimal> spent = new HashMap<>();
        for (BudgetRepository.BudgetSpent row : budgetRepository.sumSpentPerBudget(ids)) {
            spent.put(row.getBudgetId(), row.getSpent());
        }
        return spent;
    }
}

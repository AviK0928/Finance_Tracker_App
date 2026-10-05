package com.example.Finance_Tracker.Budget.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Computes how much of a budget has been spent: the sum of the owner's EXPENSE transactions
 * dated within the budget's (inclusive) start/end dates, restricted to the budget's category if it has one.
 */
@Component
public class BudgetSpendingCalculator {

    private final TransactionRepository transactionRepository;

    public BudgetSpendingCalculator(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
}

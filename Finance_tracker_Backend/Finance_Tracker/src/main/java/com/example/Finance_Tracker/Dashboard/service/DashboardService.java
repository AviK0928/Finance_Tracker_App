package com.example.Finance_Tracker.Dashboard.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.service.BudgetSpendingCalculator;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Budget.dto.BudgetResponseDTO;
import com.example.Finance_Tracker.Dashboard.dto.BudgetInfo;
import com.example.Finance_Tracker.Dashboard.dto.DashboardSummaryDTO;
import com.example.Finance_Tracker.Dashboard.dto.SummaryInfo;
import com.example.Finance_Tracker.Dashboard.dto.TransactionInfo;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import com.example.Finance_Tracker.Transaction.dto.TransactionResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetSpendingCalculator spendingCalculator;

    public DashboardSummaryDTO getDashboardSummary() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        List<Transaction> transactions = transactionRepository.findByUserId(currentUserId);
        List<Budget> budgets = budgetRepository.findByUserId(currentUserId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        List<Budget> activeBudgets = budgets.stream()
                .filter(b -> b.getStatus() == BudgetStatus.ACTIVE)
                .toList();

        List<BudgetResponseDTO> activeBudgetDTOs = activeBudgets.stream()
                .map(b -> BudgetResponseDTO.fromEntity(b, spendingCalculator.spentFor(b)))
                .collect(Collectors.toList());

        List<TransactionResponseDTO> recentTransactions = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .limit(5)
                .map(TransactionResponseDTO::fromEntity)
                .collect(Collectors.toList());

        SummaryInfo summary = SummaryInfo.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(totalIncome.subtract(totalExpense))
                .build();

        BudgetInfo budget = BudgetInfo.builder()
                .activeBudgets(activeBudgetDTOs)
                .build();

        TransactionInfo transactionsInfo = TransactionInfo.builder()
                .recentTransactions(recentTransactions)
                .build();

        return DashboardSummaryDTO.builder()
                .summary(summary)
                .budget(budget)
                .transactions(transactionsInfo)
                .build();
    }
}

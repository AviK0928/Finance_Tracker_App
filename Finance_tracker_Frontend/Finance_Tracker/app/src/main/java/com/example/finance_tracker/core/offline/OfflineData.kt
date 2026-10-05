package com.example.finance_tracker.core.offline

import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.budget.BudgetStatus
import com.example.finance_tracker.core.network.model.dashboard.BudgetInfo
import com.example.finance_tracker.core.network.model.dashboard.DashboardResponseDTO
import com.example.finance_tracker.core.network.model.dashboard.SummaryInfo
import com.example.finance_tracker.core.network.model.dashboard.TransactionInfo
import com.example.finance_tracker.core.network.model.transaction.TransactionFilterDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionType
import java.math.BigDecimal

/** What the screens show from the last synced copy when the server cannot be reached. Same rules as the server. */
object OfflineData {

    /**
     * Mirrors backend TransactionSpecification: exact category, type, date and amount bounds inclusive.
     * Newest first, like the paginated list (transactionDate desc).
     */
    fun filterTransactions(
        all: List<TransactionResponseDTO>,
        filter: TransactionFilterDTO
    ): List<TransactionResponseDTO> = all
        .filter { t ->
            (filter.category.isNullOrEmpty() || t.category == filter.category) &&
                (filter.type == null || t.type == filter.type) &&
                (filter.startDate == null || !t.transactionDate.isBefore(filter.startDate)) &&
                (filter.endDate == null || !t.transactionDate.isAfter(filter.endDate)) &&
                (filter.minAmount == null || t.amount >= filter.minAmount) &&
                (filter.maxAmount == null || t.amount <= filter.maxAmount)
        }
        .sortedWith(compareByDescending<TransactionResponseDTO> { it.transactionDate }.thenByDescending { it.id })

    /**
     * Mirrors backend DashboardService: all-time totals per type, active budgets, newest five by creation.
     * Totals are summed as BigDecimal (from each amount's decimal form) so they do not drift like Double sums.
     */
    fun dashboard(
        transactions: List<TransactionResponseDTO>,
        budgets: List<BudgetResponseDTO>
    ): DashboardResponseDTO {
        var income = BigDecimal.ZERO
        var expense = BigDecimal.ZERO
        for (t in transactions) {
            val amount = BigDecimal.valueOf(t.amount)
            if (t.type == TransactionType.INCOME) income += amount else expense += amount
        }
        val recent = transactions
            .sortedWith(compareByDescending<TransactionResponseDTO> { it.createdAt }.thenByDescending { it.id })
            .take(5)
        return DashboardResponseDTO(
            summary = SummaryInfo(totalIncome = income, totalExpense = expense, netSavings = income - expense),
            budget = BudgetInfo(activeBudgets = budgets.filter { it.budgetStatus == BudgetStatus.ACTIVE }),
            transactions = TransactionInfo(recentTransactions = recent)
        )
    }
}

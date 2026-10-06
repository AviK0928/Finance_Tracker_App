package com.example.finance_tracker.core.offline

import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.budget.BudgetStatus
import com.example.finance_tracker.core.network.model.transaction.TransactionFilterDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class OfflineDataTest {

    private val day = LocalDateTime.of(2026, 10, 5, 12, 0)

    @Test
    fun filterTransactions_appliesEveryBound_inclusive_newestFirst() {
        val all = listOf(
            txn(1, TransactionType.EXPENSE, "Food", 10.0, day.minusDays(2)),
            txn(2, TransactionType.EXPENSE, "Food", 50.0, day.minusDays(1)),
            txn(3, TransactionType.EXPENSE, "Food", 100.0, day),
            txn(4, TransactionType.INCOME, "Food", 50.0, day),
            txn(5, TransactionType.EXPENSE, "Rent", 50.0, day)
        )
        val filter = TransactionFilterDTO(
            category = "Food", type = TransactionType.EXPENSE,
            startDate = day.minusDays(1), endDate = day,
            minAmount = BigDecimal("50"), maxAmount = BigDecimal("100")
        )

        val result = OfflineData.filterTransactions(all, filter)

        // 1: before the start date; 4: income; 5: other category. Bounds 50.0, 100.0 and both dates included.
        assertEquals(listOf(3L, 2L), result.map { it.id })
    }

    @Test
    fun filterTransactions_categoryIsExact_likeTheServer() {
        val all = listOf(txn(1, TransactionType.EXPENSE, "Food", 10.0, day), txn(2, TransactionType.EXPENSE, "food", 10.0, day))

        val result = OfflineData.filterTransactions(all, TransactionFilterDTO(category = "Food"))

        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun filterTransactions_withoutFilter_returnsAllNewestFirst() {
        val all = listOf(txn(1, TransactionType.EXPENSE, "Food", 10.0, day.minusDays(1)), txn(2, TransactionType.EXPENSE, "Food", 10.0, day))

        assertEquals(listOf(2L, 1L), OfflineData.filterTransactions(all, TransactionFilterDTO()).map { it.id })
    }

    @Test
    fun dashboard_totalsWithoutDrift_activeBudgetsOnly_newestFiveByCreation() {
        val transactions = listOf(
            txn(1, TransactionType.EXPENSE, "Food", 0.1, day, created = day.minusMinutes(6)),
            txn(2, TransactionType.EXPENSE, "Food", 0.2, day, created = day.minusMinutes(5)),
            txn(3, TransactionType.INCOME, "Salary", 1000.0, day, created = day.minusMinutes(4)),
            txn(4, TransactionType.EXPENSE, "Food", 1.0, day, created = day.minusMinutes(3)),
            txn(5, TransactionType.EXPENSE, "Food", 1.0, day, created = day.minusMinutes(2)),
            txn(6, TransactionType.EXPENSE, "Food", 1.0, day, created = day.minusMinutes(1))
        )
        val budgets = listOf(budget(7, BudgetStatus.ACTIVE), budget(8, BudgetStatus.PAUSED))

        val dashboard = OfflineData.dashboard(transactions, budgets)

        // As Doubles, 0.1 + 0.2 + 1 + 1 + 1 is 3.3000000000000003
        assertEquals(0, BigDecimal("3.3").compareTo(dashboard.summary.totalExpense))
        assertEquals(0, BigDecimal("1000").compareTo(dashboard.summary.totalIncome))
        assertEquals(0, BigDecimal("996.7").compareTo(dashboard.summary.netSavings))
        assertEquals(listOf(7L), dashboard.budget.activeBudgets.map { it.id })
        assertEquals(listOf(6L, 5L, 4L, 3L, 2L), dashboard.transactions.recentTransactions.map { it.id })
    }

    private fun txn(
        id: Long, type: TransactionType, category: String, amount: Double, date: LocalDateTime,
        created: LocalDateTime = date
    ) = TransactionResponseDTO(
        id = id, userId = 1, amount = BigDecimal.valueOf(amount), category = category, type = type,
        transactionDate = date, description = null, createdAt = created, updatedAt = created
    )

    private fun budget(id: Long, status: BudgetStatus) = BudgetResponseDTO(
        id = id, userId = 1, name = "B$id", category = null, amount = BigDecimal("100.00"),
        spentAmount = BigDecimal.ZERO, remainingAmount = BigDecimal("100.00"), percentageSpent = 0.0,
        startDate = LocalDate.of(2026, 10, 1), endDate = LocalDate.of(2026, 10, 31), notes = null,
        budgetFrequency = BudgetFrequency.MONTHLY, budgetStatus = status,
        createdAt = day, updatedAt = day
    )
}

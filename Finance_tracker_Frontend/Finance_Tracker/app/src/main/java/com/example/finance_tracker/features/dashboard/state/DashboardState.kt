package com.example.finance_tracker.features.dashboard.state

import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO
import java.math.BigDecimal

data class DashboardState(
    // Summary Info
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val netSavings: BigDecimal = BigDecimal.ZERO,

    // Budget Info
    val activeBudgets: List<BudgetResponseDTO> = emptyList(),

    // Transaction Info
    val recentTransactions: List<TransactionResponseDTO> = emptyList(),

    // View/Tab State
    val currentView: DashboardView = DashboardView.SUMMARY,

    // UI State
    val isLoading: Boolean = false,
    // Showing numbers computed from the last synced copy because the server could not be reached
    val isOffline: Boolean = false,
    val error: String? = null
)
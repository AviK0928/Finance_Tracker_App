package com.example.finance_tracker.core.network.model.dashboard

data class DashboardResponseDTO(
    val summary: SummaryInfo,
    val budget: BudgetInfo,
    val transactions: TransactionInfo
)
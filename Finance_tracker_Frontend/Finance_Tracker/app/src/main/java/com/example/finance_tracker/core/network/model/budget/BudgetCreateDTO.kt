package com.example.finance_tracker.core.network.model.budget

data class BudgetCreateDTO(
    val title: String,
    val amount: Double,
    val category: String,
    val startDate: String, // Format: ISO-8601 (e.g., "2025-06-19T00:00:00")
    val endDate: String,
    val frequency: BudgetFrequency
)
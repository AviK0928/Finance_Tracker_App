package com.example.finance_tracker.core.network.model.budget

data class BudgetUpdateDTO(
    val title: String,
    val amount: Double,
    val category: String,
    val startDate: String,
    val endDate: String,
    val frequency: BudgetFrequency,
    val status: BudgetStatus
)
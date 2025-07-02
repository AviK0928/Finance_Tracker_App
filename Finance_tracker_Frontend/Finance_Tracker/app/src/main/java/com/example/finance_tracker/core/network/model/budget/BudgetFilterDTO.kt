package com.example.finance_tracker.core.network.model.budget

data class BudgetFilterDTO(
    val status: BudgetStatus? = null,
    val frequency: BudgetFrequency? = null
)
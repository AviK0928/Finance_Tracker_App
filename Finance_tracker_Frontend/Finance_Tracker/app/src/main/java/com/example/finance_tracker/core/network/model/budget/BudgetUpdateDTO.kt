package com.example.finance_tracker.core.network.model.budget

import java.time.LocalDate

// Mirrors backend Budget/dto/BudgetUpdateDTO. Dates are sent as "yyyy-MM-dd" by GsonProvider.
data class BudgetUpdateDTO(
    val name: String,
    val category: String?,
    val amount: Double,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val frequency: BudgetFrequency,
    val status: BudgetStatus,
    val notes: String? = null
)

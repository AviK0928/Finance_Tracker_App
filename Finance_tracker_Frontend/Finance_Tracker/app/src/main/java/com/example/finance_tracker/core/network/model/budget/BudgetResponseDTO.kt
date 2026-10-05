package com.example.finance_tracker.core.network.model.budget

import java.time.LocalDate
import java.time.LocalDateTime

// Mirrors backend Budget/dto/BudgetResponseDTO. Field names must match the JSON exactly (Gson).
data class BudgetResponseDTO(
    val id: Long,
    val userId: Long,
    val name: String,
    val category: String?, // optional on the backend; null means "all expense categories"
    val amount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentageSpent: Double,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val notes: String?,
    val budgetFrequency: BudgetFrequency,
    val budgetStatus: BudgetStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

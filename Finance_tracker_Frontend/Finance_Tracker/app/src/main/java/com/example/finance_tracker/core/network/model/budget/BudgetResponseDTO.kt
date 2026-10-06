package com.example.finance_tracker.core.network.model.budget

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// Mirrors backend Budget/dto/BudgetResponseDTO. Field names must match the JSON exactly (Gson).
data class BudgetResponseDTO(
    val id: Long,
    val userId: Long,
    val name: String,
    val category: String?, // optional on the backend; null means "all expense categories"
    val amount: BigDecimal,
    val spentAmount: BigDecimal,
    val remainingAmount: BigDecimal,
    val percentageSpent: Double, // a Double on the backend too (rounded percentage, not money)
    val startDate: LocalDate,
    val endDate: LocalDate,
    val notes: String?,
    val budgetFrequency: BudgetFrequency,
    val budgetStatus: BudgetStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

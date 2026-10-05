package com.example.finance_tracker.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetStatus
import java.time.LocalDate
import java.time.LocalDateTime

/** Local copy of a server budget (same fields as BudgetResponseDTO, spending as of the last sync). */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: Long,
    val userId: Long,
    val name: String,
    val category: String?,
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

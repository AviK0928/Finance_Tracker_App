package com.example.finance_tracker.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val contentHash: String,
    val amount: BigDecimal,
    val period: String,
    val updatedAt: LocalDateTime
)
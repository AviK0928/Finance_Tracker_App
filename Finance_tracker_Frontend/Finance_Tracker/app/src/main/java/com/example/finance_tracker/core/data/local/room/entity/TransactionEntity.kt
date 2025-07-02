package com.example.finance_tracker.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime


@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val contentHash: String,
    val amount: BigDecimal,
    val description: String?,
    val updatedAt: LocalDateTime
)
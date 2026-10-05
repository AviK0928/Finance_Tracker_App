package com.example.finance_tracker.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finance_tracker.core.network.model.transaction.TransactionType
import java.time.LocalDateTime

/** Local copy of a server transaction (same fields as TransactionResponseDTO), keyed by the server id. */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: Long,
    val userId: Long,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val transactionDate: LocalDateTime,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

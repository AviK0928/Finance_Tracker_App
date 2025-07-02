package com.example.finance_tracker.core.network.model.transaction

import java.time.LocalDateTime

data class TransactionResponseDTO(
    val id: Long,
    val userId: Long,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val transactionDate: LocalDateTime,
    val description: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

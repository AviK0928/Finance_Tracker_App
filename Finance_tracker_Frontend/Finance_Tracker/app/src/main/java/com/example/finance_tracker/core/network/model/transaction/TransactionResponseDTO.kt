package com.example.finance_tracker.core.network.model.transaction

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionResponseDTO(
    val id: Long,
    val userId: Long,
    val amount: BigDecimal,
    val category: String,
    val type: TransactionType,
    val transactionDate: LocalDateTime,
    val description: String?, // optional on the backend; null when not entered
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

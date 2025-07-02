package com.example.finance_tracker.core.network.model.transaction

import java.time.LocalDateTime

data class TransactionCreateDTO(
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val transactionDate: LocalDateTime,
    val description: String
)
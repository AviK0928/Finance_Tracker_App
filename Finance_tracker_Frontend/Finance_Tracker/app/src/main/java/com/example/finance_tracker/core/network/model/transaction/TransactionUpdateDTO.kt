package com.example.finance_tracker.core.network.model.transaction

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionUpdateDTO(
    val amount: BigDecimal,
    val category: String,
    val type: TransactionType,
    val transactionDate: LocalDateTime,
    val description: String
)
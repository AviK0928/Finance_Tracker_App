package com.example.finance_tracker.core.network.model.sync

import java.math.BigDecimal
import java.time.LocalDateTime


data class TransactionDTO(
    val amount: BigDecimal,
    val updatedAt: LocalDateTime,
    val description: String?,
    val contentHash: String
)
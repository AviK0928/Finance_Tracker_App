package com.example.finance_tracker.core.network.model.sync

import java.math.BigDecimal
import java.time.LocalDateTime

data class BudgetDTO(
    val amount: BigDecimal,
    val period: String,
    val updatedAt: LocalDateTime,
    val contentHash: String
)
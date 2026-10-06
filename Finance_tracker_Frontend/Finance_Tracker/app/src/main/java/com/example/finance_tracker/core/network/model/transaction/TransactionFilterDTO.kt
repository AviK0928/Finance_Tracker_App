package com.example.finance_tracker.core.network.model.transaction

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionFilterDTO(
    val category: String? = null,
    val type: TransactionType? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null
)
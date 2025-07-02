package com.example.finance_tracker.core.network.model.transaction

import java.time.LocalDateTime

data class TransactionFilterDTO(
    val category: String? = null,
    val type: TransactionType? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)
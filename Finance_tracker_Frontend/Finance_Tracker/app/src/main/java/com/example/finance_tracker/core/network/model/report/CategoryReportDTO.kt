package com.example.finance_tracker.core.network.model.report

import java.math.BigDecimal

data class CategoryReportDTO(
    val category: String,
    val totalSpent: BigDecimal
)
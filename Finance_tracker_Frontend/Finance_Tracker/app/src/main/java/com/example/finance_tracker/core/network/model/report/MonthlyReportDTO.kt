package com.example.finance_tracker.core.network.model.report

import java.math.BigDecimal

// Mirrors backend Report/dto/MonthlyReportDTO. Field names must match the JSON exactly (Gson).
data class MonthlyReportDTO(
    val year: Int,
    val month: String, // java.time.Month name, e.g. "OCTOBER"
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val netSavings: BigDecimal,
    val categoryBreakdown: Map<String, BigDecimal> // expense per category
)

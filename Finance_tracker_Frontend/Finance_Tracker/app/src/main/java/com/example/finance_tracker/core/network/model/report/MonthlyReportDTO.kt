package com.example.finance_tracker.core.network.model.report

// Mirrors backend Report/dto/MonthlyReportDTO. Field names must match the JSON exactly (Gson).
data class MonthlyReportDTO(
    val year: Int,
    val month: String, // java.time.Month name, e.g. "OCTOBER"
    val totalIncome: Double,
    val totalExpense: Double,
    val netSavings: Double,
    val categoryBreakdown: Map<String, Double> // expense per category
)

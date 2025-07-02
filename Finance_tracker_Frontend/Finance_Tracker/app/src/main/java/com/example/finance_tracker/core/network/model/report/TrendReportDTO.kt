package com.example.finance_tracker.core.network.model.report

data class TrendReportDTO(
    val label: String, // e.g., "Jan 2025", "Apr", or "Week 1"
    val income: Double,
    val expenses: Double,
    val savings: Double
)
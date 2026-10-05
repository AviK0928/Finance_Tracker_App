package com.example.finance_tracker.core.network.model.report

import java.time.LocalDateTime

// Mirrors backend Report/dto/TrendReportDTO: one entry per day that has transactions.
data class TrendReportDTO(
    val date: LocalDateTime,
    val income: Double,
    val expense: Double
)

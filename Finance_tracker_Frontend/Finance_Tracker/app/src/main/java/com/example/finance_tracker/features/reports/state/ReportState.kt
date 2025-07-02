package com.example.finance_tracker.features.reports.state

import com.example.finance_tracker.core.network.model.report.CategoryReportDTO
import com.example.finance_tracker.core.network.model.report.MonthlyReportDTO
import com.example.finance_tracker.core.network.model.report.TrendReportDTO

data class ReportState(
    val selectedMonth: String = "",  // e.g., "06"
    val selectedYear: Int = 2025,

    val monthlyReport: MonthlyReportDTO? = null,
    val categoryReport: List<CategoryReportDTO> = emptyList(),
    val trendReport: List<TrendReportDTO> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
package com.example.finance_tracker.features.reports.state

sealed class ReportEvent {
    data class LoadMonthlyReport(val month: String, val year: Int) : ReportEvent()
    object LoadCategoryReport : ReportEvent()
    data class LoadTrendReport(val period: String = "6m") : ReportEvent()

    object ClearError : ReportEvent()
}
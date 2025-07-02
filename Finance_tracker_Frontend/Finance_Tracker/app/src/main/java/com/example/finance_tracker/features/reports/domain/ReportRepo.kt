package com.example.finance_tracker.features.reports.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.report.CategoryReportDTO
import com.example.finance_tracker.core.network.model.report.MonthlyReportDTO
import com.example.finance_tracker.core.network.model.report.TrendReportDTO

interface ReportRepo {
    suspend fun getMonthlyReport(month: String, year: Int): NetworkResult<MonthlyReportDTO>
    suspend fun getCategoryReport(): NetworkResult<List<CategoryReportDTO>>
    suspend fun getTrendReport(period: String = "6m"): NetworkResult<List<TrendReportDTO>>
}
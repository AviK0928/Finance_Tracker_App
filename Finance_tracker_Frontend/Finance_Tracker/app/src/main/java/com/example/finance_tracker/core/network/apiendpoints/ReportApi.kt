package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.report.CategoryReportDTO
import com.example.finance_tracker.core.network.model.report.MonthlyReportDTO
import com.example.finance_tracker.core.network.model.report.TrendReportDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportApi {

    @GET("/api/reports/monthly")
    suspend fun getMonthlyReport(
        @Query("month") month: String, // Use uppercase short name (e.g., "JANUARY")
        @Query("year") year: Int
    ): Response<MonthlyReportDTO>

    @GET("/api/reports/category")
    suspend fun getCategoryReport(): Response<List<CategoryReportDTO>>

    @GET("/api/reports/trend")
    suspend fun getTrendReport(
        @Query("period") period: String = "6m" // e.g., "3m", "6m", "12m"
    ): Response<List<TrendReportDTO>>
}


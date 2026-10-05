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
        @Query("month") month: String, // java.time.Month name, e.g. "OCTOBER" (backend enum)
        @Query("year") year: Int
    ): Response<MonthlyReportDTO>

    @GET("/api/reports/category")
    suspend fun getCategoryReport(): Response<List<CategoryReportDTO>>

    @GET("/api/reports/trend")
    suspend fun getTrendReport(
        @Query("period") period: String = "6m" // "1m", "3m", "6m" or "1y" (anything else means 6m)
    ): Response<List<TrendReportDTO>>
}


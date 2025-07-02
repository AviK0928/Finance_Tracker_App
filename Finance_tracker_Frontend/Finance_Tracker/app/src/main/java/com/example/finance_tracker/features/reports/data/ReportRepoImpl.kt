package com.example.finance_tracker.features.reports.data

import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.RetrofitInstance
import com.example.finance_tracker.core.network.apiendpoints.ReportApi
import com.example.finance_tracker.core.network.model.report.CategoryReportDTO
import com.example.finance_tracker.core.network.model.report.MonthlyReportDTO
import com.example.finance_tracker.core.network.model.report.TrendReportDTO
import com.example.finance_tracker.features.reports.domain.ReportRepo

class ReportRepoImpl(
    tokenManager: TokenManager
) : ReportRepo {

    private val api: ReportApi = RetrofitInstance.provideReportApi(tokenManager)

    override suspend fun getMonthlyReport(month: String, year: Int): NetworkResult<MonthlyReportDTO> {
        return ApiResponseHandler.handleApi { api.getMonthlyReport(month, year) }
    }

    override suspend fun getCategoryReport(): NetworkResult<List<CategoryReportDTO>> {
        return ApiResponseHandler.handleApi { api.getCategoryReport() }
    }

    override suspend fun getTrendReport(period: String): NetworkResult<List<TrendReportDTO>> {
        return ApiResponseHandler.handleApi { api.getTrendReport(period) }
    }
}
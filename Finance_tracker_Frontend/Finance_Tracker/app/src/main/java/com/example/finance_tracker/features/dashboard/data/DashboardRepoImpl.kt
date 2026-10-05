package com.example.finance_tracker.features.dashboard.data

import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.DashboardApi
import com.example.finance_tracker.core.network.model.dashboard.DashboardResponseDTO
import com.example.finance_tracker.features.dashboard.domain.DashboardRepo

class DashboardRepoImpl(
    private val dashboardApi: DashboardApi
) : DashboardRepo {

    override suspend fun getDashboardData(): NetworkResult<DashboardResponseDTO> {
        return ApiResponseHandler.handleApi {
            dashboardApi.getDashboardData()
        }
    }
}
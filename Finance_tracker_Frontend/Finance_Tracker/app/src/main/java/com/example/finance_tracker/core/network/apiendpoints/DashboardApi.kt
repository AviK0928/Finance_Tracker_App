package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.dashboard.DashboardResponseDTO
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApi {

    @GET("/api/dashboard")
    suspend fun getDashboardData(): Response<DashboardResponseDTO>
}
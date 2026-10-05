package com.example.finance_tracker.features.dashboard.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.dashboard.DashboardResponseDTO

interface DashboardRepo {
    suspend fun getDashboardData(): NetworkResult<DashboardResponseDTO>

    /** The same numbers computed from the last synced copy; null when nothing has been synced. */
    suspend fun getLocalDashboard(): DashboardResponseDTO?
}
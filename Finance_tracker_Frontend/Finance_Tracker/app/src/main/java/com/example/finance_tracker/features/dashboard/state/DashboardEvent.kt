package com.example.finance_tracker.features.dashboard.state

sealed class DashboardEvent {
    object LoadDashboardData : DashboardEvent()
    data class ChangeView(val view: DashboardView) : DashboardEvent()
    object ClearError : DashboardEvent()
}
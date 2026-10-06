package com.example.finance_tracker.features.dashboard.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.features.dashboard.domain.DashboardRepo
import com.example.finance_tracker.core.network.model.dashboard.DashboardResponseDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepo: DashboardRepo
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.LoadDashboardData -> loadDashboardData()
            is DashboardEvent.ChangeView -> _state.update { it.copy(currentView = event.view) }
            is DashboardEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    /**
     * Pull to refresh: reloads while the current numbers stay on screen (no full-screen spinner).
     * The screen waits for the returned job, so the indicator stops exactly when the load ends.
     */
    fun refresh(): Job = loadDashboardData(showLoading = false)

    private fun loadDashboardData(showLoading: Boolean = true): Job {
        return viewModelScope.launch {
            if (showLoading) _state.update { it.copy(isLoading = true) }

            when (val result = dashboardRepo.getDashboardData()) {
                is NetworkResult.Success -> show(result.data, offline = false)

                is NetworkResult.Error -> {
                    // Server unreachable: compute the dashboard from the last synced copy if there is one
                    val local = if (result.isNoResponse) dashboardRepo.getLocalDashboard() else null
                    if (local != null) {
                        show(local, offline = true)
                    } else {
                        _state.update { it.copy(error = result.message, isLoading = false) }
                    }
                }

                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun show(data: DashboardResponseDTO, offline: Boolean) {
        _state.update {
            it.copy(
                totalIncome = data.summary.totalIncome,
                totalExpense = data.summary.totalExpense,
                netSavings = data.summary.netSavings,
                activeBudgets = data.budget.activeBudgets,
                recentTransactions = data.transactions.recentTransactions,
                isOffline = offline,
                isLoading = false,
                error = null
            )
        }
    }
}
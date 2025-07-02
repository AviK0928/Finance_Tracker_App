package com.example.finance_tracker.features.dashboard.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.features.dashboard.domain.DashboardRepo
import com.example.finance_tracker.core.network.model.dashboard.DashboardResponseDTO
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = dashboardRepo.getDashboardData()) {
                is NetworkResult.Success -> {
                    val data: DashboardResponseDTO = result.data
                    _state.update {
                        it.copy(
                            totalIncome = data.summary.totalIncome,
                            totalExpense = data.summary.totalExpense,
                            netSavings = data.summary.netSavings,
                            totalBudget = data.budget.totalBudget,
                            remainingBudget = data.budget.remainingBudget,
                            activeBudgets = data.budget.activeBudgets,
                            recentTransactions = data.transactions.recentTransactions,
                            isLoading = false,
                            error = null
                        )
                    }
                }

                is NetworkResult.Error -> _state.update {
                    it.copy(error = result.message, isLoading = false)
                }

                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }
}
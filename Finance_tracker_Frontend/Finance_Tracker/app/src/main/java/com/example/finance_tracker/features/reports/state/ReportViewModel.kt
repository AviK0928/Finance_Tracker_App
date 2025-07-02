package com.example.finance_tracker.features.reports.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.features.reports.domain.ReportRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepo: ReportRepo
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state

    fun onEvent(event: ReportEvent) {
        when (event) {
            is ReportEvent.LoadMonthlyReport -> loadMonthlyReport(event.month, event.year)
            is ReportEvent.LoadCategoryReport -> loadCategoryReport()
            is ReportEvent.LoadTrendReport -> loadTrendReport(event.period)
            is ReportEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadMonthlyReport(month: String, year: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = reportRepo.getMonthlyReport(month, year)) {
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            selectedMonth = month,
                            selectedYear = year,
                            monthlyReport = result.data,
                            isLoading = false
                        )
                    }
                }
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun loadCategoryReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = reportRepo.getCategoryReport()) {
                is NetworkResult.Success -> _state.update { it.copy(categoryReport = result.data, isLoading = false) }
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun loadTrendReport(period: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = reportRepo.getTrendReport(period)) {
                is NetworkResult.Success -> _state.update { it.copy(trendReport = result.data, isLoading = false) }
                is NetworkResult.Error -> _state.update { it.copy(errorMessage = result.message, isLoading = false) }
                NetworkResult.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }
}
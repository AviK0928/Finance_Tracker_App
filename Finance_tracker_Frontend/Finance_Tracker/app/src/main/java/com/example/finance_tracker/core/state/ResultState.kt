package com.example.finance_tracker.core.state

sealed class ResultState {
    data object Idle : ResultState()
    data object Loading : ResultState()
    data class Success(val message: String? = null) : ResultState()
    data class Error(val message: String) : ResultState()
}
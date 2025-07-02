package com.example.finance_tracker.core.state

sealed class AsyncState {
    data object Idle : AsyncState()
    data object InProgress : AsyncState()
    data class Success(val message: String? = null) : AsyncState()
    data class Error(val message: String) : AsyncState()
}
package com.example.finance_tracker.core.state

sealed class UIEvents {
    data class ShowSnackbar(val message: String) : UIEvents()
    data class Navigate(val route: String) : UIEvents()
    data object PopBackStack : UIEvents()
    data object None : UIEvents() // default no-op
}
package com.example.finance_tracker.core.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    protected val _uiEvent = MutableSharedFlow<UIEvents>()
    val uiEvent = _uiEvent.asSharedFlow()

    protected fun sendUiEvent(event: UIEvents) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
}
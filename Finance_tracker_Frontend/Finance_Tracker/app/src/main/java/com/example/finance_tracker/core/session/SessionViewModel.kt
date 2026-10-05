package com.example.finance_tracker.core.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.data.local.preferences.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Login state derived from the stored token. Login, logout, account deletion and a 401 from
 * AuthInterceptor all change the token, so navigation only has to follow this one value.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    tokenManager: TokenManager
) : ViewModel() {

    /** null until DataStore has been read, then whether a token is stored. */
    val isLoggedIn: StateFlow<Boolean?> = tokenManager.authTokens
        .map { it != null }
        .distinctUntilChanged()
        .stateIn<Boolean?>(viewModelScope, SharingStarted.Eagerly, null)
}

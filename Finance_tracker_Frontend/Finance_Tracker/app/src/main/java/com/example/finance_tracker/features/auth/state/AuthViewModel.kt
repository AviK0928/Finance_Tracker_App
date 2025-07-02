package com.example.finance_tracker.features.auth.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.data.model.AuthTokens
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.auth.ForgotPasswordRequestDTO
import com.example.finance_tracker.core.network.model.auth.LoginRequestDTO
import com.example.finance_tracker.core.network.model.auth.RegisterRequestDTO
import com.example.finance_tracker.core.network.model.auth.ResetPasswordRequestDTO
import com.example.finance_tracker.features.auth.domain.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepo,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnNameChanged -> _state.update { it.copy(name = event.name) }
            is AuthEvent.OnEmailChanged -> _state.update { it.copy(email = event.email) }
            is AuthEvent.OnPasswordChanged -> _state.update { it.copy(password = event.password) }
            is AuthEvent.ToggleAuthMode -> _state.update {
                it.copy(
                    isLoginMode = !it.isLoginMode,
                    name = "", email = "", password = "", errorMessage = null
                )
            }
            is AuthEvent.OnResetTokenChanged -> _state.update { it.copy(resetToken = event.token) }
            AuthEvent.ForgotPasswordSubmit -> forgotPasswordSubmit()
            AuthEvent.ResetPasswordSubmit -> resetPasswordSubmit()
            AuthEvent.Submit -> submit()
            is AuthEvent.ClearError -> _state.update { it.copy(errorMessage = null) }
            is AuthEvent.Submit -> submit()

        }
    }

    private fun submit() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = if (current.isLoginMode) {
                repository.login(
                    LoginRequestDTO(
                        email = current.email,
                        password = current.password
                    )
                )
            } else {
                repository.register(
                    RegisterRequestDTO(
                        username = current.name,
                        email = current.email,
                        password = current.password
                    )
                )
            }

            when (result) {
                is NetworkResult.Success -> {
                    viewModelScope.launch {
                        tokenManager.saveTokens(
                            AuthTokens(
                                accessToken = result.data.token,
                                refreshToken = result.data.token
                            )
                        )
                    }
                    _state.update { it.copy(isLoading = false, isAuthSuccessful = true) }
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                NetworkResult.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun forgotPasswordSubmit() {
        val email = _state.value.email
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.forgotPassword(ForgotPasswordRequestDTO(email))
            when (result) {
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            forgotPasswordSuccess = true,
                            forgotResetMessage = result.data
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false, forgotResetMessage = result.message)
                    }
                }
                else -> Unit
            }
        }
    }

    private fun resetPasswordSubmit() {
        val stateValue = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.resetPassword(
                ResetPasswordRequestDTO(stateValue.resetToken, stateValue.password)
            )
            when (result) {
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            resetPasswordSuccess = true,
                            forgotResetMessage = result.data
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false, forgotResetMessage = result.message)
                    }
                }
                else -> Unit
            }
        }
    }
}
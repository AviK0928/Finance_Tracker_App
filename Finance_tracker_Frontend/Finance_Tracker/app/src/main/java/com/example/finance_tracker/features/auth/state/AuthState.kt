package com.example.finance_tracker.features.auth.state

data class AuthState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginMode: Boolean = true,
    val errorMessage: String? = null,
    val isAuthSuccessful: Boolean = false,
    val resetToken: String = "",
    val forgotPasswordSuccess: Boolean = false,
    val resetPasswordSuccess: Boolean = false,
    val forgotResetMessage: String? = null,
    val sessionExpired: Boolean = false
)
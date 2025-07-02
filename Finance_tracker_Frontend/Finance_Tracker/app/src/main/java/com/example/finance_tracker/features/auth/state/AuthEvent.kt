package com.example.finance_tracker.features.auth.state

sealed class AuthEvent {
    data class OnNameChanged(val name: String) : AuthEvent()
    data class OnEmailChanged(val email: String) : AuthEvent()
    data class OnPasswordChanged(val password: String) : AuthEvent()
    object ToggleAuthMode : AuthEvent()
    object Submit : AuthEvent()
    object ClearError : AuthEvent()
    object ForgotPasswordSubmit : AuthEvent()
    data class OnResetTokenChanged(val token: String) : AuthEvent()
    object ResetPasswordSubmit : AuthEvent()
}
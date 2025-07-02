package com.example.finance_tracker.core.network.model.auth

data class ResetPasswordRequestDTO(
    val token: String,
    val newPassword: String
)
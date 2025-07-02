package com.example.finance_tracker.core.network.model.auth

data class LoginRequestDTO(
    val email: String,
    val password: String
)
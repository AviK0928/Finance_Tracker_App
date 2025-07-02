package com.example.finance_tracker.core.network.model.auth

data class RegisterRequestDTO(
    val username: String,
    val email: String,
    val password: String
)
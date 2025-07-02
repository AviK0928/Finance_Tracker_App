package com.example.finance_tracker.features.auth.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.auth.AuthResponseDTO
import com.example.finance_tracker.core.network.model.auth.ForgotPasswordRequestDTO
import com.example.finance_tracker.core.network.model.auth.LoginRequestDTO
import com.example.finance_tracker.core.network.model.auth.RegisterRequestDTO
import com.example.finance_tracker.core.network.model.auth.ResetPasswordRequestDTO

interface AuthRepo {
    suspend fun register(request: RegisterRequestDTO): NetworkResult<AuthResponseDTO>
    suspend fun login(request: LoginRequestDTO): NetworkResult<AuthResponseDTO>
    suspend fun forgotPassword(request: ForgotPasswordRequestDTO): NetworkResult<String>
    suspend fun resetPassword(request: ResetPasswordRequestDTO): NetworkResult<String>
}
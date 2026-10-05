package com.example.finance_tracker.features.auth.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.auth.AuthResponseDTO
import com.example.finance_tracker.core.network.model.auth.ForgotPasswordRequestDTO
import com.example.finance_tracker.core.network.model.auth.MessageResponseDTO
import com.example.finance_tracker.core.network.model.auth.LoginRequestDTO
import com.example.finance_tracker.core.network.model.auth.RegisterRequestDTO
import com.example.finance_tracker.core.network.model.auth.ResetPasswordRequestDTO
import kotlinx.coroutines.flow.Flow

interface AuthRepo {
    suspend fun register(request: RegisterRequestDTO): NetworkResult<AuthResponseDTO>
    suspend fun login(request: LoginRequestDTO): NetworkResult<AuthResponseDTO>
    suspend fun forgotPassword(request: ForgotPasswordRequestDTO): NetworkResult<MessageResponseDTO>
    suspend fun resetPassword(request: ResetPasswordRequestDTO): NetworkResult<MessageResponseDTO>

    /** True after the server ended the session (401), until the next login or a dismiss. */
    val sessionExpired: Flow<Boolean>
    suspend fun dismissSessionExpired()
}
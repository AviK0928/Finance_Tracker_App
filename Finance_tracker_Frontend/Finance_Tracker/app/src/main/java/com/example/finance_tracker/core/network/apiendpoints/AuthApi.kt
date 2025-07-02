package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.auth.AuthResponseDTO
import com.example.finance_tracker.core.network.model.auth.ForgotPasswordRequestDTO
import com.example.finance_tracker.core.network.model.auth.LoginRequestDTO
import com.example.finance_tracker.core.network.model.auth.RegisterRequestDTO
import com.example.finance_tracker.core.network.model.auth.ResetPasswordRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/auth/register")
    suspend fun registerUser(
        @Body request: RegisterRequestDTO
    ): Response<AuthResponseDTO>

    @POST("/api/auth/login")
    suspend fun loginUser(
        @Body request: LoginRequestDTO
    ): Response<AuthResponseDTO>

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDTO): Response<String>

    @POST("/api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDTO): Response<String>
}
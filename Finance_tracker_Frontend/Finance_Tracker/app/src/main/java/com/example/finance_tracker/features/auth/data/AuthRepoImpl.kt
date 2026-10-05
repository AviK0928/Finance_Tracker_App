package com.example.finance_tracker.features.auth.data

import com.example.finance_tracker.core.network.RetrofitInstance
import com.example.finance_tracker.core.network.apiendpoints.AuthApi
import com.example.finance_tracker.core.network.model.auth.AuthResponseDTO
import com.example.finance_tracker.core.network.model.auth.LoginRequestDTO
import com.example.finance_tracker.core.network.model.auth.RegisterRequestDTO
import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.data.model.AuthTokens
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.auth.ForgotPasswordRequestDTO
import com.example.finance_tracker.core.network.model.auth.MessageResponseDTO
import com.example.finance_tracker.core.network.model.auth.ResetPasswordRequestDTO
import com.example.finance_tracker.features.auth.domain.AuthRepo

class AuthRepoImpl(
    private val tokenManager: TokenManager
) : AuthRepo {

    private val authApi: AuthApi = RetrofitInstance.provideAuthApi(tokenManager)

    override suspend fun register(request: RegisterRequestDTO): NetworkResult<AuthResponseDTO> {
        val response = ApiResponseHandler.handleApi { authApi.registerUser(request) }
        if (response is NetworkResult.Success) {
            tokenManager.saveTokens(AuthTokens(response.data.token, response.data.token))
        }
        return response
    }

    override suspend fun login(request: LoginRequestDTO): NetworkResult<AuthResponseDTO> {
        val response = ApiResponseHandler.handleApi { authApi.loginUser(request) }
        if (response is NetworkResult.Success) {
            tokenManager.saveTokens(AuthTokens(response.data.token, response.data.token))
        }
        return response
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequestDTO): NetworkResult<MessageResponseDTO> {
        return ApiResponseHandler.handleApi { authApi.forgotPassword(request) }
    }

    override suspend fun resetPassword(request: ResetPasswordRequestDTO): NetworkResult<MessageResponseDTO> {
        return ApiResponseHandler.handleApi { authApi.resetPassword(request) }
    }
}
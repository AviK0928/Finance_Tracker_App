package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.data.local.preferences.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getAccessToken() }
            ?: return chain.proceed(chain.request()) // not logged in: send without Authorization

        val request = chain.request()
        val response = chain.proceed(
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        )

        if (endsSession(response.code, request.url.encodedPath)) {
            // The server no longer accepts this token (expired, blacklisted, user deleted).
            // Clearing it makes AppNavGraph show the login screen.
            runBlocking { tokenManager.clearTokensIfCurrent(token) }
        }
        return response
    }

    companion object {
        /**
         * A 401 means the stored token was rejected, except under /api/auth/ where the backend
         * also answers 401 for a wrong email or password.
         */
        fun endsSession(code: Int, path: String): Boolean =
            code == 401 && !path.startsWith("/api/auth/")
    }
}

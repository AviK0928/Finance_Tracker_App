package com.example.finance_tracker.core.data.local.preferences

import kotlinx.coroutines.flow.first
import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.finance_tracker.core.data.model.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.finance_tracker.core.data.local.preferences.UserPreferencesKeys.ACCESS_TOKEN
import com.example.finance_tracker.core.data.local.preferences.UserPreferencesKeys.REFRESH_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val authTokens: Flow<AuthTokens?> = context.dataStore.data.map { preferences ->
        val accessToken = preferences[ACCESS_TOKEN]
        val refreshToken = preferences[REFRESH_TOKEN]
        if (accessToken != null && refreshToken != null) {
            AuthTokens(accessToken, refreshToken)
        } else null
    }

    suspend fun saveTokens(tokens: AuthTokens) {
        context.dataStore.edit {
            it[ACCESS_TOKEN] = tokens.accessToken
            it[REFRESH_TOKEN] = tokens.refreshToken
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(REFRESH_TOKEN)
        }
    }

    /** Returns the stored access token, or null when the user is not logged in. */
    suspend fun getAccessToken(): String? {
        return context.dataStore.data.first()[ACCESS_TOKEN]
    }

}


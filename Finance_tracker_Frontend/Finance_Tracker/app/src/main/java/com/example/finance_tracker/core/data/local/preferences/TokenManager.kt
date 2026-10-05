package com.example.finance_tracker.core.data.local.preferences

import kotlinx.coroutines.flow.first
import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.finance_tracker.core.data.model.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.finance_tracker.core.data.local.preferences.UserPreferencesKeys.ACCESS_TOKEN
import com.example.finance_tracker.core.data.local.preferences.UserPreferencesKeys.REFRESH_TOKEN
import com.example.finance_tracker.core.data.local.preferences.UserPreferencesKeys.SESSION_EXPIRED
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

    /** True after the server rejected the stored token, until the next login or a dismiss. */
    val sessionExpired: Flow<Boolean> = context.dataStore.data.map { it[SESSION_EXPIRED] ?: false }

    suspend fun saveTokens(tokens: AuthTokens) {
        context.dataStore.edit {
            it[ACCESS_TOKEN] = tokens.accessToken
            it[REFRESH_TOKEN] = tokens.refreshToken
            it.remove(SESSION_EXPIRED)
        }
    }

    /** Normal logout or account deletion: no "session expired" notice. */
    suspend fun clearTokens() {
        context.dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(REFRESH_TOKEN)
            it.remove(SESSION_EXPIRED)
        }
    }

    /**
     * Clears the session only if [accessToken] is still the stored token. One DataStore edit, so it is
     * atomic: a late 401 from a request sent before a re-login cannot log the new session out.
     */
    suspend fun clearTokensIfCurrent(accessToken: String) {
        context.dataStore.edit {
            if (it[ACCESS_TOKEN] == accessToken) {
                it.remove(ACCESS_TOKEN)
                it.remove(REFRESH_TOKEN)
                it[SESSION_EXPIRED] = true
            }
        }
    }

    suspend fun dismissSessionExpired() {
        context.dataStore.edit { it.remove(SESSION_EXPIRED) }
    }

    /** Returns the stored access token, or null when the user is not logged in. */
    suspend fun getAccessToken(): String? {
        return context.dataStore.data.first()[ACCESS_TOKEN]
    }

}


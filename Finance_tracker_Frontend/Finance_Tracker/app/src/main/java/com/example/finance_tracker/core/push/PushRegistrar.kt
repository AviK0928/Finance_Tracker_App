package com.example.finance_tracker.core.push

import com.example.finance_tracker.core.di_config.ApplicationScope
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.apiendpoints.DeviceApi
import com.example.finance_tracker.core.network.model.device.DeviceRegistrationDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the backend's device_tokens row for this install in step with the session:
 * - a session starts (app start with a stored token, or a login): register the current FCM token;
 * - FCM issues a new token while logged in: register it;
 * - logout: [unregister] removes the row on the server; it must run while the JWT is still stored;
 * - a session ends (logout, account deletion, a 401): delete the FCM token on the phone, so a row the
 *   server could not be told about (offline logout, expired JWT) fails with UNREGISTERED on the next
 *   send and the backend drops it. Starting logged out does nothing.
 * Every call is best effort: push must never block login or logout.
 */
@Singleton
class PushRegistrar @Inject constructor(
    private val deviceApi: DeviceApi,
    private val tokenSource: PushTokenSource,
    @ApplicationScope private val scope: CoroutineScope
) {

    @Volatile
    private var loggedIn = false

    fun follow(isLoggedIn: Flow<Boolean>) {
        scope.launch {
            var wasLoggedIn = false
            isLoggedIn.distinctUntilChanged().collect { now ->
                loggedIn = now
                if (now) {
                    register(tokenSource.currentToken())
                } else if (wasLoggedIn) {
                    tokenSource.deleteToken()
                }
                wasLoggedIn = now
            }
        }
    }

    /** Called by FCM when the token changes. Ignored while logged out (the next login registers it). */
    fun onNewToken(token: String) {
        if (loggedIn) {
            scope.launch { register(token) }
        }
    }

    /**
     * Removes this install's token from the logged-in account. Call before the JWT is cleared.
     * Gives up after [UNREGISTER_TIMEOUT_MS] so an offline logout is not held up by network timeouts.
     */
    suspend fun unregister() {
        withTimeoutOrNull(UNREGISTER_TIMEOUT_MS) {
            tokenSource.currentToken()?.let { token ->
                ApiResponseHandler.handleApi { deviceApi.unregister(token) }
            }
        }
    }

    private suspend fun register(token: String?) {
        if (token == null) return
        ApiResponseHandler.handleApi { deviceApi.register(DeviceRegistrationDTO(token)) }
    }

    companion object {
        const val UNREGISTER_TIMEOUT_MS = 3_000L
    }
}

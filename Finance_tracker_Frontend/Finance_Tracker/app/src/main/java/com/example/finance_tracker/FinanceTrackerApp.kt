package com.example.finance_tracker

import android.app.Application
import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.sync.SessionSyncCoordinator
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltAndroidApp
class FinanceTrackerApp : Application() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var sessionSyncCoordinator: SessionSyncCoordinator

    override fun onCreate() {
        super.onCreate() // Hilt injects the fields here
        // App-wide, not tied to a screen: a 401 from any request also ends the session
        sessionSyncCoordinator.follow(tokenManager.authTokens.map { it != null })
    }
}
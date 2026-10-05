package com.example.finance_tracker.core.data.local.preferences


import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    // Set when the server rejected the token (not on a normal logout); shown on the login screen
    val SESSION_EXPIRED = booleanPreferencesKey("session_expired")
    val THEME = stringPreferencesKey("theme") // e.g., light/dark/system
}
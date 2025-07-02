package com.example.finance_tracker.core.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.finance_tracker.core.data.local.preferences.UserPreferencesKeys.ONBOARDING_COMPLETED
import com.example.finance_tracker.core.data.local.preferences.UserPreferencesKeys.THEME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val USER_PREFS_NAME = "user_preferences"

val Context.dataStore by preferencesDataStore(name = USER_PREFS_NAME)

class UserPreferences(private val context: Context) {

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { it[ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }

    val themePreference: Flow<String> = context.dataStore.data
        .map { it[THEME] ?: "system" }

    suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME] = theme
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}


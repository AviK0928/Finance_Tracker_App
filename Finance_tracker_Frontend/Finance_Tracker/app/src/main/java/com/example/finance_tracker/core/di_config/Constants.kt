package com.example.finance_tracker.core.di_config

import com.example.finance_tracker.BuildConfig

object Constants {
    /** Per build type, see app/build.gradle.kts (debug: financeTracker.baseUrl in local.properties). */
    val BASE_URL: String = BuildConfig.BASE_URL
    const val PREFS_NAME = "user_preferences"
}
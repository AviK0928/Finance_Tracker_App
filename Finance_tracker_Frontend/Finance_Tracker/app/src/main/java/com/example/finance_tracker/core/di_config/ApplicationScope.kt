package com.example.finance_tracker.core.di_config

import javax.inject.Qualifier

/** The app-wide CoroutineScope (outlives every screen and ViewModel). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

package com.example.finance_tracker.features.settings.state

import com.example.finance_tracker.core.network.model.settings.UserSettingDTO
import java.time.LocalDateTime

data class SettingsState(
    val settings: List<UserSettingDTO> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // Export ZIP waiting for the user to pick a save location; cleared once handled
    val pendingExport: ByteArray? = null,

    // Sync-specific fields
    val lastSync: LocalDateTime? = null,
    val isSyncing: Boolean = false
)

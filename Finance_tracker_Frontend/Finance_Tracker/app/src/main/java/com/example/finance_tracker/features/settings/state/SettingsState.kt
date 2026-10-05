package com.example.finance_tracker.features.settings.state

import com.example.finance_tracker.core.network.model.settings.UserSettingDTO
import com.example.finance_tracker.core.network.model.sync.SyncMetadataDTO
import java.time.LocalDateTime

data class SettingsState(
    val settings: List<UserSettingDTO> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,

    // Export ZIP waiting for the user to pick a save location; cleared once handled
    val pendingExport: ByteArray? = null,

    // Sync-specific fields
    val syncMetadata: SyncMetadataDTO? = null,
    val lastSync: LocalDateTime? = null,
    val isSyncing: Boolean = false
)

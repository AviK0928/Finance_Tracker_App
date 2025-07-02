package com.example.finance_tracker.core.network.model.sync

import java.time.LocalDateTime


data class SyncRequestDTO(
    val lastSync: LocalDateTime,
    val manualSync: Boolean
)
package com.example.finance_tracker.core.network.model.sync

import java.time.LocalDateTime


data class SyncMetadataDTO(
    val latestBudgetUpdate: LocalDateTime,
    val latestTransactionUpdate: LocalDateTime
)
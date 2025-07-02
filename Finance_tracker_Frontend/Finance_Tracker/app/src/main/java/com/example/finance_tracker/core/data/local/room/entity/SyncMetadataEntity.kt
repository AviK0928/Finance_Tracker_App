package com.example.finance_tracker.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val id: Int = 0, // only 1 row expected
    val latestBudgetUpdate: LocalDateTime,
    val latestTransactionUpdate: LocalDateTime
)


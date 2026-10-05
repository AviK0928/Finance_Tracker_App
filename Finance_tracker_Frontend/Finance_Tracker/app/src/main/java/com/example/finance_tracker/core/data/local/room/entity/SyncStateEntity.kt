package com.example.finance_tracker.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The server's sync cursor (one row). Lives in the same database as the synced data, so a sync
 * applies data and cursor in one transaction and clearing the database also forgets the cursor.
 */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val id: Int = 0,
    val cursor: String
)

package com.example.finance_tracker.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finance_tracker.core.data.local.room.entity.SyncStateEntity

@Dao
interface SyncStateDao {

    /** Null before the first sync and after the database was cleared: the next sync is a full one. */
    @Query("SELECT cursor FROM sync_state WHERE id = 0")
    suspend fun getCursor(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: SyncStateEntity)
}

package com.example.finance_tracker.core.data.local.room.dao

import androidx.room.*
import com.example.finance_tracker.core.data.local.room.entity.SyncMetadataEntity

@Dao
interface SyncMetadataDao {

    @Query("SELECT * FROM sync_metadata LIMIT 1")
    suspend fun getSyncMetadata(): SyncMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(metadata: SyncMetadataEntity)
}
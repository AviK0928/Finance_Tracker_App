package com.example.finance_tracker.core.data.local.room.dao

import androidx.room.*
import com.example.finance_tracker.core.data.local.room.entity.UserSettingEntity
import com.example.finance_tracker.core.network.model.settings.SettingKey


@Dao
interface UserSettingDao {

    @Query("SELECT * FROM user_settings")
    suspend fun getAllSettings(): List<UserSettingEntity>

    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    suspend fun getSettingByKey(key: SettingKey): UserSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(settings: List<UserSettingEntity>)
}
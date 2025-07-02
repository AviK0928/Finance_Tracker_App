package com.example.finance_tracker.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finance_tracker.core.network.model.settings.SettingKey

@Entity(tableName = "user_settings")
data class UserSettingEntity(
    @PrimaryKey val key: SettingKey,
    val value: String
)
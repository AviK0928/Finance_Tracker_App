package com.example.finance_tracker.features.settings.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.settings.UpdateSettingDTO
import com.example.finance_tracker.core.network.model.settings.UserSettingDTO

interface SettingsRepo {
    suspend fun getSettings(): NetworkResult<List<UserSettingDTO>>
    suspend fun updateSettings(settings: List<UpdateSettingDTO>): NetworkResult<Unit>
    suspend fun resetToDefaults(): NetworkResult<Unit>
    suspend fun logout(): NetworkResult<Unit>
    suspend fun deleteAccount(): NetworkResult<Unit>
    suspend fun exportData(): NetworkResult<ByteArray>
    suspend fun importData(file: ByteArray, filename: String): NetworkResult<Unit>
}
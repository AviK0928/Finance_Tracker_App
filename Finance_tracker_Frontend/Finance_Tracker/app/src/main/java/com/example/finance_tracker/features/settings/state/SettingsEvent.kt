package com.example.finance_tracker.features.settings.state

import com.example.finance_tracker.core.network.model.settings.UpdateSettingDTO

sealed class SettingsEvent {
    object LoadSettings : SettingsEvent()
    data class UpdateSettings(val settings: List<UpdateSettingDTO>) : SettingsEvent()
    object ResetToDefaults : SettingsEvent()
    object Logout : SettingsEvent()
    object DeleteAccount : SettingsEvent()
    object ExportData : SettingsEvent()
    data class ImportData(val file: ByteArray, val filename: String) : SettingsEvent()
    object PerformSync : SettingsEvent()
    object ClearError : SettingsEvent()
    object ClearInfo : SettingsEvent()
    data class ExportSaved(val saved: Boolean) : SettingsEvent()
}
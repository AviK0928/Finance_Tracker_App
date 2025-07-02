package com.example.finance_tracker.core.network.model.settings

data class UpdateSettingDTO(
    val key: SettingKey,
    val value: String
)
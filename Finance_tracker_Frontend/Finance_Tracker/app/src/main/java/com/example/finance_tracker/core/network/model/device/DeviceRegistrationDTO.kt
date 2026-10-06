package com.example.finance_tracker.core.network.model.device

/** Body of POST /api/devices (backend Device/dto/DeviceRegistrationDTO). */
data class DeviceRegistrationDTO(
    val token: String,
    val platform: String = PLATFORM_ANDROID
) {
    companion object {
        const val PLATFORM_ANDROID = "ANDROID"
    }
}

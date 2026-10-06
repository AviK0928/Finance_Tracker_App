package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.network.model.device.DeviceRegistrationDTO
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceContractTest {

    @Test
    fun registrationJson_matchesBackendDto() {
        // Backend Device/dto/DeviceRegistrationDTO: token (not blank) and platform (enum DevicePlatform)
        val json = GsonProvider.gson.toJson(DeviceRegistrationDTO("abc:123"))

        assertEquals("""{"token":"abc:123","platform":"ANDROID"}""", json)
    }
}

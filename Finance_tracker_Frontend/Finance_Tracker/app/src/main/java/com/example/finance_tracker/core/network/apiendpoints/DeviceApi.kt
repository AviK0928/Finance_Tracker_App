package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.device.DeviceRegistrationDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceApi {

    /** Registers this install's FCM token for the logged-in user (204). */
    @POST("/api/devices")
    suspend fun register(@Body body: DeviceRegistrationDTO): Response<Unit>

    /** Removes the token if it belongs to the logged-in user; always 204. */
    @DELETE("/api/devices/{token}")
    suspend fun unregister(@Path("token") token: String): Response<Unit>
}

package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.settings.UpdateSettingDTO
import com.example.finance_tracker.core.network.model.settings.UserSettingDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface SettingsApi {

    @GET("/api/settings")
    suspend fun getSettings(): Response<List<UserSettingDTO>>

    @PUT("/api/settings")
    suspend fun updateSettings(
        @Body settings: List<UpdateSettingDTO>
    ): Response<Unit>

    @POST("/api/settings/reset-to-defaults")
    suspend fun resetToDefaults(): Response<Unit>

    @POST("/api/settings/logout")
    suspend fun logout(): Response<Unit>

    @DELETE("/api/settings/delete-account")
    suspend fun deleteAccount(): Response<Unit>

    @GET("/api/settings/export-data")
    suspend fun exportData(): Response<ByteArray>

    @Multipart
    @POST("/api/settings/import-data")
    suspend fun importData(
        @Part file: MultipartBody.Part
    ): Response<Unit>
}
package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.sync.SyncRequestDTO
import com.example.finance_tracker.core.network.model.sync.SyncResponseDTO
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApi {

    @POST("/api/sync")
    suspend fun syncData(
        @Body request: SyncRequestDTO
    ): Response<SyncResponseDTO>

}
package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.sync.SyncResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SyncApi {

    /** A null cursor is left out of the URL, which asks for a full sync. */
    @GET("/api/sync")
    suspend fun sync(@Query("cursor") cursor: String?): Response<SyncResponseDTO>
}

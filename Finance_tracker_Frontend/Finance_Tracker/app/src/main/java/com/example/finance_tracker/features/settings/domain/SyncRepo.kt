package com.example.finance_tracker.features.settings.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.sync.SyncMetadataDTO
import com.example.finance_tracker.core.network.model.sync.SyncRequestDTO
import com.example.finance_tracker.core.network.model.sync.SyncResponseDTO

interface SyncRepo {
    suspend fun syncData(request: SyncRequestDTO): NetworkResult<SyncResponseDTO>
}
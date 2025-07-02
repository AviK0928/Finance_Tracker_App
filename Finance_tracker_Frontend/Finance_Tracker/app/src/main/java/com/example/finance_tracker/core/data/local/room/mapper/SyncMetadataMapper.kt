package com.example.finance_tracker.core.data.local.room.mapper

import com.example.finance_tracker.core.data.local.room.entity.SyncMetadataEntity
import com.example.finance_tracker.core.network.model.sync.SyncMetadataDTO

object SyncMetadataMapper {
    fun fromDTO(dto: SyncMetadataDTO): SyncMetadataEntity {
        return SyncMetadataEntity(
            latestBudgetUpdate = dto.latestBudgetUpdate,
            latestTransactionUpdate = dto.latestTransactionUpdate
        )
    }
}
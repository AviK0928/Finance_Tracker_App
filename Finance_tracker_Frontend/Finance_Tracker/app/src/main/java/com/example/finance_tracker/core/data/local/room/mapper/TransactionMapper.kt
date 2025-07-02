package com.example.finance_tracker.core.data.local.room.mapper

import com.example.finance_tracker.core.data.local.room.entity.TransactionEntity
import com.example.finance_tracker.core.network.model.sync.TransactionDTO

object TransactionMapper {
    fun fromDTO(dto: TransactionDTO): TransactionEntity {
        return TransactionEntity(
            amount = dto.amount,
            updatedAt = dto.updatedAt,
            description = dto.description ?: "",
            contentHash = dto.contentHash
        )
    }

    fun fromDTOList(dtos: List<TransactionDTO>): List<TransactionEntity> {
        return dtos.map { fromDTO(it) }
    }
}
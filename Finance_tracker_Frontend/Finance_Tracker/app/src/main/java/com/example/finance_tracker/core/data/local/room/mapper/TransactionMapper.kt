package com.example.finance_tracker.core.data.local.room.mapper

import com.example.finance_tracker.core.data.local.room.entity.TransactionEntity
import com.example.finance_tracker.core.network.model.transaction.TransactionResponseDTO

object TransactionMapper {
    fun fromDTO(dto: TransactionResponseDTO): TransactionEntity = TransactionEntity(
        id = dto.id,
        userId = dto.userId,
        amount = dto.amount,
        category = dto.category,
        type = dto.type,
        transactionDate = dto.transactionDate,
        description = dto.description,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt
    )

    fun toDTO(entity: TransactionEntity): TransactionResponseDTO = TransactionResponseDTO(
        id = entity.id,
        userId = entity.userId,
        amount = entity.amount,
        category = entity.category,
        type = entity.type,
        transactionDate = entity.transactionDate,
        description = entity.description,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )
}

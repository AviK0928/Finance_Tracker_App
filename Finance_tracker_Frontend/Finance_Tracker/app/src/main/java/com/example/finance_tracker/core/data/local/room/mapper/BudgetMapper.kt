package com.example.finance_tracker.core.data.local.room.mapper

import com.example.finance_tracker.core.data.local.room.entity.BudgetEntity
import com.example.finance_tracker.core.network.model.sync.BudgetDTO

object BudgetMapper {
    fun fromDTO(dto: BudgetDTO): BudgetEntity {
        return BudgetEntity(
            amount = dto.amount,
            period = dto.period,
            updatedAt = dto.updatedAt,
            contentHash = dto.contentHash
        )
    }

    fun fromDTOList(dtos: List<BudgetDTO>): List<BudgetEntity> {
        return dtos.map { fromDTO(it) }
    }
}
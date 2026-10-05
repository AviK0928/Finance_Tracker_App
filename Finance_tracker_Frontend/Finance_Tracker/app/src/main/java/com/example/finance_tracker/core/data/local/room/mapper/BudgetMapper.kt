package com.example.finance_tracker.core.data.local.room.mapper

import com.example.finance_tracker.core.data.local.room.entity.BudgetEntity
import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO

object BudgetMapper {
    fun fromDTO(dto: BudgetResponseDTO): BudgetEntity = BudgetEntity(
        id = dto.id,
        userId = dto.userId,
        name = dto.name,
        category = dto.category,
        amount = dto.amount,
        spentAmount = dto.spentAmount,
        remainingAmount = dto.remainingAmount,
        percentageSpent = dto.percentageSpent,
        startDate = dto.startDate,
        endDate = dto.endDate,
        notes = dto.notes,
        budgetFrequency = dto.budgetFrequency,
        budgetStatus = dto.budgetStatus,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt
    )

    fun toDTO(entity: BudgetEntity): BudgetResponseDTO = BudgetResponseDTO(
        id = entity.id,
        userId = entity.userId,
        name = entity.name,
        category = entity.category,
        amount = entity.amount,
        spentAmount = entity.spentAmount,
        remainingAmount = entity.remainingAmount,
        percentageSpent = entity.percentageSpent,
        startDate = entity.startDate,
        endDate = entity.endDate,
        notes = entity.notes,
        budgetFrequency = entity.budgetFrequency,
        budgetStatus = entity.budgetStatus,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )
}

package com.example.finance_tracker.features.budgets.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.budget.*
import com.example.finance_tracker.core.network.model.sync.BudgetDTO

interface BudgetRepo {
    suspend fun createBudget(dto: BudgetCreateDTO): NetworkResult<BudgetResponseDTO>
    suspend fun updateBudget(id: Long, dto: BudgetUpdateDTO): NetworkResult<BudgetResponseDTO>
    suspend fun deleteBudget(id: Long): NetworkResult<Unit>
    suspend fun getBudgetById(id: Long): NetworkResult<BudgetResponseDTO>
    suspend fun getBudgetsByUser(): NetworkResult<List<BudgetResponseDTO>>
    suspend fun exportBudgetsAsPdf(
        status: BudgetStatus? = null,
        frequency: BudgetFrequency? = null
    ): NetworkResult<ByteArray>
    suspend fun getLocalBudgets(): List<BudgetDTO>
}
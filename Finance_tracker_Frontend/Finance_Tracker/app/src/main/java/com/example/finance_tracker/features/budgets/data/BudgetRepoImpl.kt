package com.example.finance_tracker.features.budgets.data

import com.example.finance_tracker.core.data.local.room.dao.BudgetDao
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.BudgetApi
import com.example.finance_tracker.core.network.model.budget.*
import com.example.finance_tracker.features.budgets.domain.BudgetRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class BudgetRepoImpl @Inject constructor(
    private val api: BudgetApi,
    private val budgetDao: BudgetDao
) : BudgetRepo {

    override suspend fun createBudget(dto: BudgetCreateDTO): NetworkResult<BudgetResponseDTO> {
        return ApiResponseHandler.handleApi { api.createBudget(dto) }
    }

    override suspend fun updateBudget(id: Long, dto: BudgetUpdateDTO): NetworkResult<BudgetResponseDTO> {
        return ApiResponseHandler.handleApi { api.updateBudget(id, dto) }
    }

    override suspend fun deleteBudget(id: Long): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.deleteBudget(id) }.also {
            // Room deletion is handled during sync
        }
    }

    override suspend fun getBudgetById(id: Long): NetworkResult<BudgetResponseDTO> {
        return ApiResponseHandler.handleApi { api.getBudgetById(id) }
    }

    override suspend fun getBudgetsByUser(): NetworkResult<List<BudgetResponseDTO>> {
        return ApiResponseHandler.handleApi { api.getBudgetsByUser() }
    }

    override suspend fun exportBudgetsAsPdf(
        status: BudgetStatus?,
        frequency: BudgetFrequency?
    ): NetworkResult<ByteArray> = withContext(Dispatchers.IO) {
        when (val result = ApiResponseHandler.handleApi { api.exportBudgetsAsPdf(status, frequency) }) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.use { it.bytes() })
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }
}
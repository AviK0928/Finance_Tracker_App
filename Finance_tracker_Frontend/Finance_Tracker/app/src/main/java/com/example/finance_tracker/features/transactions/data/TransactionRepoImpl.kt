package com.example.finance_tracker.features.transactions.data

import com.example.finance_tracker.core.data.local.room.dao.TransactionDao
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.apiendpoints.TransactionApi
import com.example.finance_tracker.core.network.model.transaction.*
import com.example.finance_tracker.core.sync.SyncTrigger
import com.example.finance_tracker.features.transactions.domain.TransactionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRepoImpl(
    private val api: TransactionApi,
    private val transactionDao: TransactionDao,
    private val syncTrigger: SyncTrigger
) : TransactionRepo {

    override suspend fun createTransaction(dto: TransactionCreateDTO): NetworkResult<TransactionResponseDTO> {
        return ApiResponseHandler.handleApi { api.createTransaction(dto) }.alsoSyncOnSuccess()
    }

    override suspend fun getTransactionById(id: Long): NetworkResult<TransactionResponseDTO> {
        return ApiResponseHandler.handleApi { api.getTransactionById(id) }
    }

    override suspend fun getAllTransactionsForUser(): NetworkResult<List<TransactionResponseDTO>> {
        return ApiResponseHandler.handleApi { api.getAllTransactionsForUser() }
    }

    override suspend fun getCategories(): NetworkResult<List<String>> {
        return ApiResponseHandler.handleApi { api.getCategories() }
    }

    override suspend fun getFilteredTransactions(
        category: String?,
        type: TransactionType?,
        startDate: String?,
        endDate: String?,
        minAmount: Double?,
        maxAmount: Double?
    ): NetworkResult<List<TransactionResponseDTO>> {
        return ApiResponseHandler.handleApi {
            api.getFilteredTransactions(category, type, startDate, endDate, minAmount, maxAmount)
        }
    }

    override suspend fun updateTransaction(id: Long, dto: TransactionUpdateDTO): NetworkResult<TransactionResponseDTO> {
        return ApiResponseHandler.handleApi { api.updateTransaction(id, dto) }.alsoSyncOnSuccess()
    }

    override suspend fun deleteTransaction(id: Long): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.deleteTransaction(id) }.alsoSyncOnSuccess()
    }

    /** A change made online also refreshes the offline copy (the server is the source of truth). */
    private fun <T> NetworkResult<T>.alsoSyncOnSuccess(): NetworkResult<T> = also {
        if (it is NetworkResult.Success) syncTrigger.requestSync()
    }

    override suspend fun getFilteredTransactionsPaginated(
        filter: TransactionFilterDTO,
        page: Int,
        size: Int,
        sort: List<String>
    ): NetworkResult<PaginatedTransactionResponse> {
        return ApiResponseHandler.handleApi {
            api.getFilteredTransactionsPaginated(filter, page, size, sort)
        }
    }

    override suspend fun exportFilteredTransactionsToPDF(
        filter: TransactionFilterDTO
    ): NetworkResult<ByteArray> = withContext(Dispatchers.IO) {
        when (val result = ApiResponseHandler.handleApi { api.exportFilteredTransactionsToPDF(filter) }) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.use { it.bytes() })
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }
}
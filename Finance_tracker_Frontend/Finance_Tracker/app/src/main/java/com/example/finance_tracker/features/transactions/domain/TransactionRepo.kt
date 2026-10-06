package com.example.finance_tracker.features.transactions.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.transaction.*
import java.math.BigDecimal

interface TransactionRepo {
    suspend fun createTransaction(dto: TransactionCreateDTO): NetworkResult<TransactionResponseDTO>
    suspend fun getTransactionById(id: Long): NetworkResult<TransactionResponseDTO>
    suspend fun getAllTransactionsForUser(): NetworkResult<List<TransactionResponseDTO>>
    suspend fun getCategories(): NetworkResult<List<String>>
    suspend fun getFilteredTransactions(
        category: String? = null,
        type: TransactionType? = null,
        startDate: String? = null,
        endDate: String? = null,
        minAmount: BigDecimal? = null,
        maxAmount: BigDecimal? = null
    ): NetworkResult<List<TransactionResponseDTO>>

    suspend fun updateTransaction(id: Long, dto: TransactionUpdateDTO): NetworkResult<TransactionResponseDTO>
    suspend fun deleteTransaction(id: Long): NetworkResult<Unit>

    /** Last synced copy filtered like the server does; null when nothing has been synced. */
    suspend fun getLocalTransactions(filter: TransactionFilterDTO): List<TransactionResponseDTO>?
    suspend fun getFilteredTransactionsPaginated(
        filter: TransactionFilterDTO,
        page: Int = 0,
        size: Int = 10,
        sort: List<String> = listOf("transactionDate,desc")
    ): NetworkResult<PaginatedTransactionResponse>

    suspend fun exportFilteredTransactionsToPDF(
        filter: TransactionFilterDTO
    ): NetworkResult<ByteArray>
}
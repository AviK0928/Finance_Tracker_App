package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.transaction.*
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    @POST("/api/transactions")
    suspend fun createTransaction(
        @Body dto: TransactionCreateDTO
    ): Response<TransactionResponseDTO>

    @GET("/api/transactions/{id}")
    suspend fun getTransactionById(
        @Path("id") id: Long
    ): Response<TransactionResponseDTO>

    @GET("/api/transactions/user")
    suspend fun getAllTransactionsForUser(): Response<List<TransactionResponseDTO>>

    @GET("/api/transactions/filter")
    suspend fun getFilteredTransactions(
        @Query("category") category: String? = null,
        @Query("type") type: TransactionType? = null,
        @Query("startDate") startDate: String? = null, // ISO 8601
        @Query("endDate") endDate: String? = null,
        @Query("minAmount") minAmount: Double? = null,
        @Query("maxAmount") maxAmount: Double? = null
    ): Response<List<TransactionResponseDTO>>

    @PUT("/api/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: Long,
        @Body dto: TransactionUpdateDTO
    ): Response<TransactionResponseDTO>

    @DELETE("/api/transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: Long
    ): Response<Unit>

    @POST("/api/transactions/filter/paginated")
    suspend fun getFilteredTransactionsPaginated(
        @Body filter: TransactionFilterDTO,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: List<String> = listOf("transactionDate,desc")
    ): Response<PaginatedTransactionResponse>

    @POST("/api/transactions/export/pdf")
    suspend fun exportFilteredTransactionsToPDF(
        @Body filter: TransactionFilterDTO
    ): Response<ByteArray>
}
package com.example.finance_tracker.core.network.apiendpoints

import com.example.finance_tracker.core.network.model.budget.BudgetCreateDTO
import com.example.finance_tracker.core.network.model.budget.BudgetResponseDTO
import com.example.finance_tracker.core.network.model.budget.BudgetUpdateDTO
import com.example.finance_tracker.core.network.model.budget.BudgetFrequency
import com.example.finance_tracker.core.network.model.budget.BudgetStatus
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface BudgetApi {

    @POST("/api/budgets")
    suspend fun createBudget(
        @Body dto: BudgetCreateDTO
    ): Response<BudgetResponseDTO>

    @PUT("/api/budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: Long,
        @Body dto: BudgetUpdateDTO
    ): Response<BudgetResponseDTO>

    @DELETE("/api/budgets/{id}")
    suspend fun deleteBudget(
        @Path("id") id: Long
    ): Response<Unit>

    @GET("/api/budgets/{id}")
    suspend fun getBudgetById(
        @Path("id") id: Long
    ): Response<BudgetResponseDTO>

    @GET("/api/budgets/user")
    suspend fun getBudgetsByUser(): Response<List<BudgetResponseDTO>>

    // Raw PDF bytes: ResponseBody is passed through by Retrofit; ByteArray would go through Gson and fail
    @GET("/api/budgets/export/pdf")
    suspend fun exportBudgetsAsPdf(
        @Query("status") status: BudgetStatus? = null,
        @Query("frequency") frequency: BudgetFrequency? = null
    ): Response<ResponseBody>
}


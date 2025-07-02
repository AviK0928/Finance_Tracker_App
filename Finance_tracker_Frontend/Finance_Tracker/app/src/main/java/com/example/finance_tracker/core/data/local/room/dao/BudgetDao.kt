package com.example.finance_tracker.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finance_tracker.core.data.local.room.entity.BudgetEntity

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE contentHash = :contentHash")
    suspend fun getBudgetByContentHash(contentHash: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets WHERE contentHash NOT IN (:hashes)")
    suspend fun deleteMissingBudgets(hashes: List<String>)
}
package com.example.finance_tracker.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.finance_tracker.core.data.local.room.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY id")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY id")
    suspend fun getAll(): List<BudgetEntity>

    @Insert
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}

package com.example.finance_tracker.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.finance_tracker.core.data.local.room.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /** Newest first. ISO-8601 text sorts chronologically. */
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

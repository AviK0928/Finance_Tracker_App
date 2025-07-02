package com.example.finance_tracker.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.*
import com.example.finance_tracker.core.data.local.room.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE contentHash = :contentHash")
    suspend fun getTransactionByContentHash(contentHash: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE contentHash NOT IN (:hashes)")
    suspend fun deleteMissingTransactions(hashes: List<String>)
}
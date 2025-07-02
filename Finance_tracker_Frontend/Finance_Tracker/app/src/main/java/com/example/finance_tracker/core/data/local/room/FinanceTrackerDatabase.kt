package com.example.finance_tracker.core.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.finance_tracker.core.data.local.room.dao.BudgetDao
import com.example.finance_tracker.core.data.local.room.dao.SyncMetadataDao
import com.example.finance_tracker.core.data.local.room.dao.TransactionDao
import com.example.finance_tracker.core.data.local.room.dao.UserSettingDao
import com.example.finance_tracker.core.data.local.room.entity.BudgetEntity
import com.example.finance_tracker.core.data.local.room.entity.SyncMetadataEntity
import com.example.finance_tracker.core.data.local.room.entity.TransactionEntity
import com.example.finance_tracker.core.data.local.room.entity.UserSettingEntity

@Database(
    entities = [
        BudgetEntity::class,
        TransactionEntity::class,
        UserSettingEntity::class,
        SyncMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class FinanceTrackerDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userSettingDao(): UserSettingDao
    abstract fun syncMetadataDao(): SyncMetadataDao
}
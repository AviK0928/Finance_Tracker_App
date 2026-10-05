package com.example.finance_tracker.core.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.finance_tracker.core.data.local.room.dao.BudgetDao
import com.example.finance_tracker.core.data.local.room.dao.SyncStateDao
import com.example.finance_tracker.core.data.local.room.dao.TransactionDao
import com.example.finance_tracker.core.data.local.room.dao.UserSettingDao
import com.example.finance_tracker.core.data.local.room.entity.BudgetEntity
import com.example.finance_tracker.core.data.local.room.entity.SyncStateEntity
import com.example.finance_tracker.core.data.local.room.entity.TransactionEntity
import com.example.finance_tracker.core.data.local.room.entity.UserSettingEntity

// Version 2: transactions and budgets keyed by server id with full fields; sync_state replaces sync_metadata.
// A cache only: DatabaseModule falls back to a destructive migration.
@Database(
    entities = [
        BudgetEntity::class,
        TransactionEntity::class,
        UserSettingEntity::class,
        SyncStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class FinanceTrackerDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userSettingDao(): UserSettingDao
    abstract fun syncStateDao(): SyncStateDao
}

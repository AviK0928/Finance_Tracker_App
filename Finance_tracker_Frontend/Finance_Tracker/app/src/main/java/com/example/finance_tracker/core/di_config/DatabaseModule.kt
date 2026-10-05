package com.example.finance_tracker.core.di_config

import android.content.Context
import androidx.room.Room
import com.example.finance_tracker.core.data.local.room.FinanceTrackerDatabase
import com.example.finance_tracker.core.data.local.room.dao.BudgetDao
import com.example.finance_tracker.core.data.local.room.dao.SyncStateDao
import com.example.finance_tracker.core.data.local.room.dao.TransactionDao
import com.example.finance_tracker.core.data.local.room.dao.UserSettingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFinanceTrackerDatabase(
        @dagger.hilt.android.qualifiers.ApplicationContext appContext: Context
    ): FinanceTrackerDatabase {
        return Room.databaseBuilder(
            appContext,
            FinanceTrackerDatabase::class.java,
            "finance_tracker_db"
        )
            // Everything in this database is a cache of server data: on a schema change, drop it
            // and let the next full sync refill it instead of crashing on a missing migration.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideBudgetDao(db: FinanceTrackerDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideTransactionDao(db: FinanceTrackerDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideUserSettingDao(db: FinanceTrackerDatabase): UserSettingDao = db.userSettingDao()
    @Provides fun provideSyncStateDao(db: FinanceTrackerDatabase): SyncStateDao = db.syncStateDao()
}
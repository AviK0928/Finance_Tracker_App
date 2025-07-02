package com.example.finance_tracker.core.di_config

import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.data.local.room.dao.BudgetDao
import com.example.finance_tracker.core.data.local.room.dao.SyncMetadataDao
import com.example.finance_tracker.core.data.local.room.dao.TransactionDao
import com.example.finance_tracker.core.data.local.room.dao.UserSettingDao
import com.example.finance_tracker.features.auth.data.AuthRepoImpl
import com.example.finance_tracker.features.auth.domain.AuthRepo
import com.example.finance_tracker.features.budgets.data.BudgetRepoImpl
import com.example.finance_tracker.features.budgets.domain.BudgetRepo
import com.example.finance_tracker.features.dashboard.data.DashboardRepoImpl
import com.example.finance_tracker.features.dashboard.domain.DashboardRepo
import com.example.finance_tracker.features.notification.data.NotificationRepoImpl
import com.example.finance_tracker.features.notification.data.NotificationWebSocketManagerImpl
import com.example.finance_tracker.features.notification.domain.NotificationRepo
import com.example.finance_tracker.features.notification.domain.NotificationWebSocketManager
import com.example.finance_tracker.features.reports.data.ReportRepoImpl
import com.example.finance_tracker.features.reports.domain.ReportRepo
import com.example.finance_tracker.features.settings.data.SettingsRepoImpl
import com.example.finance_tracker.features.settings.data.SyncRepoImpl
import com.example.finance_tracker.features.settings.domain.SettingsRepo
import com.example.finance_tracker.features.settings.domain.SyncRepo
import com.example.finance_tracker.features.transactions.data.TransactionRepoImpl
import com.example.finance_tracker.features.transactions.domain.TransactionRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Bindings will be added here (e.g., TokenManager, UserPreferences, Network, DB, etc.)
    @Provides
    @Singleton
    fun provideAuthRepo(
        tokenManager: TokenManager
    ): AuthRepo = AuthRepoImpl(tokenManager)

    @Provides
    @Singleton
    fun provideBudgetRepo(
        tokenManager: TokenManager,
        budgetDao: BudgetDao
    ): BudgetRepo = BudgetRepoImpl(tokenManager, budgetDao)

    @Provides
    @Singleton
    fun provideDashboardRepo(
        tokenManager: TokenManager
    ): DashboardRepo = DashboardRepoImpl(tokenManager)

    @Provides
    @Singleton
    fun provideNotificationRepo(
        tokenManager: TokenManager
    ): NotificationRepo = NotificationRepoImpl(tokenManager)

    @Provides
    @Singleton
    fun provideNotificationWebSocketManager(): NotificationWebSocketManager =
        NotificationWebSocketManagerImpl()

    @Provides
    @Singleton
    fun provideReportRepo(
        tokenManager: TokenManager
    ): ReportRepo = ReportRepoImpl(tokenManager)

    @Provides
    @Singleton
    fun provideSettingsRepo(
        tokenManager: TokenManager,
        userSettingDao: UserSettingDao
    ): SettingsRepo = SettingsRepoImpl(tokenManager, userSettingDao)

    @Provides
    @Singleton
    fun provideSyncRepo(
        tokenManager: TokenManager,
        budgetDao: BudgetDao,
        transactionDao: TransactionDao,
        userSettingDao: UserSettingDao,
        syncMetadataDao: SyncMetadataDao
    ): SyncRepo = SyncRepoImpl(
        tokenManager,
        budgetDao,
        transactionDao,
        userSettingDao,
        syncMetadataDao
    )

    @Provides
    @Singleton
    fun provideTransactionRepo(
        tokenManager: TokenManager,
        transactionDao: TransactionDao
    ): TransactionRepo = TransactionRepoImpl(tokenManager, transactionDao)

}
package com.example.finance_tracker.core.di_config

import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.data.local.room.dao.BudgetDao
import com.example.finance_tracker.core.data.local.room.FinanceTrackerDatabase
import com.example.finance_tracker.core.data.local.room.dao.TransactionDao
import com.example.finance_tracker.core.data.local.room.dao.UserSettingDao
import com.example.finance_tracker.core.network.apiendpoints.AuthApi
import com.example.finance_tracker.core.network.apiendpoints.BudgetApi
import com.example.finance_tracker.core.network.apiendpoints.DashboardApi
import com.example.finance_tracker.core.network.apiendpoints.NotificationApi
import com.example.finance_tracker.core.network.apiendpoints.ReportApi
import com.example.finance_tracker.core.network.apiendpoints.SettingsApi
import com.example.finance_tracker.core.network.apiendpoints.SyncApi
import com.example.finance_tracker.core.network.apiendpoints.TransactionApi
import com.example.finance_tracker.features.auth.data.AuthRepoImpl
import com.example.finance_tracker.features.auth.domain.AuthRepo
import com.example.finance_tracker.features.budgets.data.BudgetRepoImpl
import com.example.finance_tracker.features.budgets.domain.BudgetRepo
import com.example.finance_tracker.features.dashboard.data.DashboardRepoImpl
import com.example.finance_tracker.features.dashboard.domain.DashboardRepo
import com.example.finance_tracker.features.notification.data.NotificationRepoImpl
import com.example.finance_tracker.features.notification.domain.NotificationRepo
import com.example.finance_tracker.features.reports.data.ReportRepoImpl
import com.example.finance_tracker.features.reports.domain.ReportRepo
import com.example.finance_tracker.features.settings.data.SettingsRepoImpl
import com.example.finance_tracker.features.settings.domain.SettingsRepo
import com.example.finance_tracker.core.sync.SyncRepo
import com.example.finance_tracker.core.sync.SessionSyncCoordinator
import com.example.finance_tracker.core.sync.SyncRepoImpl
import com.example.finance_tracker.core.sync.SyncTrigger
import com.example.finance_tracker.features.transactions.data.TransactionRepoImpl
import com.example.finance_tracker.features.transactions.domain.TransactionRepo
import dagger.Module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Repositories. Network singletons (OkHttp, Retrofit, APIs) come from NetworkModule.
    @Provides
    @Singleton
    fun provideAuthRepo(
        authApi: AuthApi,
        tokenManager: TokenManager
    ): AuthRepo = AuthRepoImpl(authApi, tokenManager)

    @Provides
    @Singleton
    fun provideBudgetRepo(
        budgetApi: BudgetApi,
        budgetDao: BudgetDao,
        syncTrigger: SyncTrigger
    ): BudgetRepo = BudgetRepoImpl(budgetApi, budgetDao, syncTrigger)

    @Provides
    @Singleton
    fun provideDashboardRepo(
        dashboardApi: DashboardApi
    ): DashboardRepo = DashboardRepoImpl(dashboardApi)

    @Provides
    @Singleton
    fun provideNotificationRepo(
        notificationApi: NotificationApi
    ): NotificationRepo = NotificationRepoImpl(notificationApi)

    @Provides
    @Singleton
    fun provideReportRepo(
        reportApi: ReportApi
    ): ReportRepo = ReportRepoImpl(reportApi)

    @Provides
    @Singleton
    fun provideSettingsRepo(
        settingsApi: SettingsApi,
        tokenManager: TokenManager,
        userSettingDao: UserSettingDao
    ): SettingsRepo = SettingsRepoImpl(settingsApi, tokenManager, userSettingDao)

    @Provides
    @Singleton
    fun provideSyncRepo(
        syncApi: SyncApi,
        database: FinanceTrackerDatabase
    ): SyncRepo = SyncRepoImpl(syncApi, database)

    @Provides
    @Singleton
    fun provideTransactionRepo(
        transactionApi: TransactionApi,
        transactionDao: TransactionDao,
        syncTrigger: SyncTrigger
    ): TransactionRepo = TransactionRepoImpl(transactionApi, transactionDao, syncTrigger)

    /** Lives as long as the app; SupervisorJob so one failed job does not cancel the others. */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    fun provideSyncTrigger(coordinator: SessionSyncCoordinator): SyncTrigger = coordinator

}
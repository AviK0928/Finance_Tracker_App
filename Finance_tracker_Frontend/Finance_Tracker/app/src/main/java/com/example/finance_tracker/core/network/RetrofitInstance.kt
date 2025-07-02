package com.example.finance_tracker.core.network

import com.example.finance_tracker.core.di_config.Constants
import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.network.apiendpoints.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun provideRetrofit(tokenManager: TokenManager): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(provideOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideAuthApi(tokenManager: TokenManager): AuthApi {
        return provideRetrofit(tokenManager).create(AuthApi::class.java)
    }

    fun provideBudgetApi(tokenManager: TokenManager): BudgetApi{
        return provideRetrofit(tokenManager).create(BudgetApi::class.java)
    }

    fun provideDashboardApi(tokenManager: TokenManager): DashboardApi{
        return provideRetrofit(tokenManager).create(DashboardApi::class.java)
    }

    fun provideReportApi(tokenManager: TokenManager): ReportApi{
        return provideRetrofit(tokenManager).create(ReportApi::class.java)
    }

    fun provideTransactionApi(tokenManager: TokenManager): TransactionApi{
        return provideRetrofit(tokenManager).create(TransactionApi::class.java)
    }

    fun provideNotificationApi(tokenManager: TokenManager): NotificationApi {
        return provideRetrofit(tokenManager).create(NotificationApi::class.java)
    }

    fun provideSettingsApi(tokenManager: TokenManager): SettingsApi {
        return provideRetrofit(tokenManager).create(SettingsApi::class.java)
    }

    fun provideSyncApi(tokenManager: TokenManager): SyncApi {
        return provideRetrofit(tokenManager).create(SyncApi::class.java)
    }
}
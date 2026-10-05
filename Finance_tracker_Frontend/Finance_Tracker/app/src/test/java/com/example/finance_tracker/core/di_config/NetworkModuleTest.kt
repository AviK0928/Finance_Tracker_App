package com.example.finance_tracker.core.di_config

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NetworkModuleTest {

    @Test
    fun retrofit_usesTheSharedClientAndBaseUrl() {
        val client = OkHttpClient()

        val retrofit = NetworkModule.provideRetrofit(client)

        // Every API is created from this Retrofit, so they all share one connection pool
        assertSame(client, retrofit.callFactory())
        assertEquals(Constants.BASE_URL, retrofit.baseUrl().toString())
    }
}

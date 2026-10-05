package com.example.finance_tracker.core.network

import java.io.IOException

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val throwable: Throwable? = null
    ) : NetworkResult<Nothing>() {
        /** No HTTP response at all (no network, server unreachable): the case where cached data helps. */
        val isNoResponse: Boolean get() = throwable is IOException
    }
    object Loading : NetworkResult<Nothing>()
}
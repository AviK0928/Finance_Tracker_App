package com.example.finance_tracker.core.network

data class BaseResponse<T>(
    val status: String? = null,
    val message: String? = null,
    val data: T? = null
)
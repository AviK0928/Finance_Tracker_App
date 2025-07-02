package com.example.finance_tracker.features.notification.data

import com.example.finance_tracker.core.data.local.preferences.TokenManager
import com.example.finance_tracker.core.network.ApiResponseHandler
import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.RetrofitInstance
import com.example.finance_tracker.core.network.apiendpoints.NotificationApi
import com.example.finance_tracker.core.network.model.notification.NotificationDTO
import com.example.finance_tracker.features.notification.domain.NotificationRepo

class NotificationRepoImpl(
    tokenManager: TokenManager
) : NotificationRepo {

    private val api: NotificationApi = RetrofitInstance.provideNotificationApi(tokenManager)

    override suspend fun getNotifications(): NetworkResult<List<NotificationDTO>> {
        return ApiResponseHandler.handleApi { api.getNotifications() }
    }

    override suspend fun getUnreadCount(): NetworkResult<Long> {
        return ApiResponseHandler.handleApi { api.getUnreadCount() }
    }

    override suspend fun markAsRead(id: Long): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.markAsRead(id) }
    }

    override suspend fun markAllAsRead(): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.markAllAsRead() }
    }

    override suspend fun delete(id: Long): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.delete(id) }
    }

    override suspend fun deleteBulk(ids: List<Long>): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.deleteBulk(ids) }
    }

    override suspend fun archive(id: Long): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.archive(id) }
    }

    override suspend fun archiveBulk(ids: List<Long>): NetworkResult<Unit> {
        return ApiResponseHandler.handleApi { api.archiveBulk(ids) }
    }
}
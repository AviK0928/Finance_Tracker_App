package com.example.finance_tracker.features.notification.domain

import com.example.finance_tracker.core.network.NetworkResult
import com.example.finance_tracker.core.network.model.notification.NotificationDTO

interface NotificationRepo {
    suspend fun getNotifications(): NetworkResult<List<NotificationDTO>>
    suspend fun getUnreadCount(): NetworkResult<Long>
    suspend fun markAsRead(id: Long): NetworkResult<Unit>
    suspend fun markAllAsRead(): NetworkResult<Unit>
    suspend fun delete(id: Long): NetworkResult<Unit>
    suspend fun deleteBulk(ids: List<Long>): NetworkResult<Unit>
    suspend fun archive(id: Long): NetworkResult<Unit>
    suspend fun archiveBulk(ids: List<Long>): NetworkResult<Unit>
}
package com.example.finance_tracker.features.notification.domain

import com.example.finance_tracker.core.network.model.notification.NotificationDTO
import kotlinx.coroutines.flow.SharedFlow

interface NotificationWebSocketManager {
    fun start(userId: Long)
    fun stop()
    val newNotifications: SharedFlow<NotificationDTO>
}
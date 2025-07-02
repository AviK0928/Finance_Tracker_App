package com.example.finance_tracker.features.notification.state

import com.example.finance_tracker.core.network.model.notification.NotificationDTO


data class NotificationState(
    val notifications: List<NotificationDTO> = emptyList(),
    val unreadCount: Long = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedNotifications: Set<Long> = emptySet()
)
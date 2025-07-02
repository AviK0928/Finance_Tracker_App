package com.example.finance_tracker.core.network.model.notification

import java.time.LocalDateTime

data class NotificationDTO(
    val id: Long,
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: LocalDateTime,
    val read: Boolean,
    val referenceId: Long?
)
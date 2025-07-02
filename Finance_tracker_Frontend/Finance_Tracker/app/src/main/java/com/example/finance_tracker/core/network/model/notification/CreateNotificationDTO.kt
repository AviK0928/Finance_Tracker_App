package com.example.finance_tracker.core.network.model.notification

data class CreateNotificationDTO(
    val title: String,
    val message: String,
    val type: NotificationType,
    val referenceId: Long? = null
)
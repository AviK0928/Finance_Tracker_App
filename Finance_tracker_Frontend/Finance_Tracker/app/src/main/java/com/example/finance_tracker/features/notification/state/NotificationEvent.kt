package com.example.finance_tracker.features.notification.state

sealed class NotificationEvent {
    object LoadAll : NotificationEvent()
    object MarkAllAsRead : NotificationEvent()
    data class MarkAsRead(val id: Long) : NotificationEvent()
    data class Delete(val id: Long) : NotificationEvent()
    data class DeleteBulk(val ids: List<Long>) : NotificationEvent()
    data class Archive(val id: Long) : NotificationEvent()
    data class ArchiveBulk(val ids: List<Long>) : NotificationEvent()
    object ClearError : NotificationEvent()

    data class ToggleSelection(val id: Long) : NotificationEvent()
    data class SetSelection(val ids: Set<Long>) : NotificationEvent()
    object ClearSelection : NotificationEvent()
}
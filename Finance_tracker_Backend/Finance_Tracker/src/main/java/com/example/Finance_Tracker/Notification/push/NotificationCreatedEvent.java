package com.example.Finance_Tracker.Notification.push;

/** Published after a notification is saved; the push goes out once the saving transaction has committed. */
public record NotificationCreatedEvent(Long userId) {
}

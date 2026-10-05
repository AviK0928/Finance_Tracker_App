package com.example.Finance_Tracker.Notification.controller;

import com.example.Finance_Tracker.Notification.dto.NotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications() {
        return ResponseEntity.ok(notificationService.getNotificationsByUser());
    }

    @PostMapping("/{id}/mark-as-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mark-all-as-read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archiveNotification(@PathVariable Long id) {
        notificationService.archiveNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/archive")
    public ResponseEntity<Void> archiveNotifications(@RequestBody List<Long> ids) {
        notificationService.archiveNotifications(ids);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteNotifications(@RequestBody List<Long> ids) {
        notificationService.deleteNotifications(ids);
        return ResponseEntity.noContent().build();
    }
}
package com.example.Finance_Tracker.Notification.service;

import com.example.Finance_Tracker.Core.websockets.NotificationWebSocketPublisher;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Notification.dto.NotificationDTO;
import com.example.Finance_Tracker.Notification.entity.Notification;
import com.example.Finance_Tracker.Notification.mapper.NotificationMapper;
import com.example.Finance_Tracker.Notification.repository.NotificationRepository;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationWebSocketPublisher notificationWebSocketPublisher;

    public List<NotificationDTO> getNotificationsByUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return notificationRepository.findByUserId(userId)
                .stream()
                .map(NotificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Notification createNotification(CreateNotificationDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .title(dto.getTitle())
                .message(dto.getMessage())
                .type(dto.getType())
                .read(false)
                .referenceId(dto.getReferenceId())
                .archived(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        NotificationDTO notificationDTO = NotificationMapper.toDTO(saved);
        notificationWebSocketPublisher.sendNotification(userId, notificationDTO);

        return saved;
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void deleteNotification(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalStateException("Unauthorized access");
        }

        notificationRepository.delete(notification);
    }

    public void archiveNotification(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalStateException("Unauthorized access");
        }

        notification.setArchived(true);
        notificationRepository.save(notification);
    }

    public void archiveNotifications(List<Long> ids) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Notification> notifications = notificationRepository.findAllById(ids).stream()
                .filter(n -> n.getUserId().equals(userId))
                .toList();

        for (Notification notification : notifications) {
            notification.setArchived(true);
        }

        notificationRepository.saveAll(notifications);
    }

    public void deleteNotifications(List<Long> ids) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Notification> notifications = notificationRepository.findAllById(ids).stream()
                .filter(n -> n.getUserId().equals(userId))
                .toList();

        notificationRepository.deleteAll(notifications);
    }

    public boolean existsByTitleAndDateAndUserId(String title, Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);
        return notificationRepository.existsByUserIdAndTitleAndCreatedAtBetween(userId, title, start, end);
    }

    public Notification createNotificationForUser(Long userId, CreateNotificationDTO dto) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(dto.getTitle())
                .message(dto.getMessage())
                .type(dto.getType())
                .read(false)
                .referenceId(dto.getReferenceId())
                .archived(false)
                .build();
        Notification saved = notificationRepository.save(notification);

        NotificationDTO notificationDTO = NotificationMapper.toDTO(saved);
        notificationWebSocketPublisher.sendNotification(userId, notificationDTO);

        return saved;
    }
}
package com.example.Finance_Tracker.Notification.service;

import com.example.Finance_Tracker.Core.exception.ResourceNotFoundException;
import com.example.Finance_Tracker.Core.websockets.NotificationWebSocketPublisher;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Notification.dto.NotificationDTO;
import com.example.Finance_Tracker.Notification.entity.Notification;
import com.example.Finance_Tracker.Notification.mapper.NotificationMapper;
import com.example.Finance_Tracker.Notification.repository.NotificationRepository;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import com.example.Finance_Tracker.Settings.service.UserSettingService;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    @Autowired
    private UserSettingService userSettingService;

    public List<NotificationDTO> getNotificationsByUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return notificationRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /** Same as {@link #createNotificationForUser} for the logged-in user. */
    public Notification createNotification(CreateNotificationDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return createNotificationForUser(userId, dto);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = getOwnedNotification(notificationId);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return notificationRepository.countByUserIdAndReadFalseAndArchivedFalse(userId);
    }

    public void deleteNotification(Long id) {
        Notification notification = getOwnedNotification(id);
        notificationRepository.delete(notification);
    }

    public void archiveNotification(Long id) {
        Notification notification = getOwnedNotification(id);
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

    /**
     * Loads a notification and verifies it belongs to the current user.
     * 404 if it doesn't exist, 403 if it belongs to someone else.
     */
    private Notification getOwnedNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!notification.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Notification " + id + " does not belong to the current user");
        }
        return notification;
    }

    public boolean existsByTitleAndDateAndUserId(String title, Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);
        return notificationRepository.existsByUserIdAndTitleAndCreatedAtBetween(userId, title, start, end);
    }

    /**
     * Saves and pushes a notification unless the user muted it (master switch or {@code dto.preference}).
     * Callers continue as if it was sent, so a muted alert is not re-sent later.
     *
     * @return the saved notification, or {@code null} when muted
     */
    public Notification createNotificationForUser(Long userId, CreateNotificationDTO dto) {
        if (!isWanted(userId, dto.getPreference())) {
            return null;
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

    private boolean isWanted(Long userId, SettingKey preference) {
        if (!userSettingService.getBooleanForUser(userId, SettingKey.NOTIFICATIONS_ENABLED)) {
            return false;
        }
        return preference == null || userSettingService.getBooleanForUser(userId, preference);
    }
}
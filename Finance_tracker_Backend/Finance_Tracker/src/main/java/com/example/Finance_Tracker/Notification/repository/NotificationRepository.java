package com.example.Finance_Tracker.Notification.repository;

import com.example.Finance_Tracker.Notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /** The user's inbox: archived notifications are hidden, newest first. */
    List<Notification> findByUserIdAndArchivedFalseOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndReadFalse(Long userId);
    long countByUserIdAndReadFalseAndArchivedFalse(Long userId);
    void deleteByUserId(Long userId); // optional cleanup
    boolean existsByUserIdAndTitleAndCreatedAtBetween(Long userId, String title, LocalDateTime start, LocalDateTime end);
}
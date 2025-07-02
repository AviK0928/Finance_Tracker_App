package com.example.Finance_Tracker.Notification.repository;

import com.example.Finance_Tracker.Notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
    void deleteByUserId(Long userId); // optional cleanup
    boolean existsByUserIdAndTitleAndCreatedAtBetween(Long userId, String title, LocalDateTime start, LocalDateTime end);
}
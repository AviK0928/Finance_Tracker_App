package com.example.Finance_Tracker.Notification.dto;

import com.example.Finance_Tracker.Notification.util.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private Long id;

    private String title;

    private String message;

    private NotificationType type;

    private LocalDateTime createdAt;

    private boolean read;

    private Long referenceId;
}

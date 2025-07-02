package com.example.Finance_Tracker.Notification.mapper;

import com.example.Finance_Tracker.Notification.dto.NotificationDTO;
import com.example.Finance_Tracker.Notification.entity.Notification;

public class NotificationMapper {

    public static NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReferenceId(notification.getReferenceId());
        return dto;
    }
}

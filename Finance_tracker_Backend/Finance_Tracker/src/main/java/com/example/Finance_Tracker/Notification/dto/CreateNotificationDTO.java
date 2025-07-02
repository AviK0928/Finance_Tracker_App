package com.example.Finance_Tracker.Notification.dto;

import com.example.Finance_Tracker.Notification.util.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNotificationDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private NotificationType type;

    private Long referenceId;
}

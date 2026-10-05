package com.example.Finance_Tracker.Notification.dto;

import com.example.Finance_Tracker.Notification.util.NotificationType;
import com.example.Finance_Tracker.Settings.util.SettingKey;
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

    /**
     * User setting that can mute this kind of notification (e.g. NOTIFY_SPENDING_ALERTS).
     * Null means only the master switch NOTIFICATIONS_ENABLED applies.
     */
    private SettingKey preference;
}

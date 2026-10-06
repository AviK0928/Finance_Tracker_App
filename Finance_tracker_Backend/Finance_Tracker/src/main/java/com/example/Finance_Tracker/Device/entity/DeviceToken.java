package com.example.Finance_Tracker.Device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Firebase registration token of one signed-in app install (one row per token). */
@Entity
@Table(name = "device_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 4096)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public DeviceToken(Long userId, String token, DevicePlatform platform, LocalDateTime now) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The same token registered again, possibly by another account on the same phone: it now belongs to that user. */
    public void assignTo(Long userId, DevicePlatform platform, LocalDateTime now) {
        this.userId = userId;
        this.platform = platform;
        this.updatedAt = now;
    }
}

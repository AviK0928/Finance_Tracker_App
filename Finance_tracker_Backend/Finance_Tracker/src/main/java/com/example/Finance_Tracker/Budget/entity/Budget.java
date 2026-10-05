package com.example.Finance_Tracker.Budget.entity;

import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Budget.util.BudgetUsageAlertStage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    private String name;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    /** Optional. Null means the budget covers all expense categories. */
    private String category;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private BudgetFrequency frequency;

    @Enumerated(EnumType.STRING)
    private BudgetStatus status;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetUsageAlertStage lastNotifiedStage;

    @Column(nullable = false)
    private boolean expiryNotificationSent = false;

    @Column(nullable = false)
    private boolean nearingExpiryNotificationSent = false;

    @Column(nullable = false, length = 64)
    private String contentHash;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = BudgetStatus.ACTIVE;
        if (frequency == null) frequency = BudgetFrequency.NONE;
        if (lastNotifiedStage == null) lastNotifiedStage = BudgetUsageAlertStage.NONE;
        expiryNotificationSent = false;
        nearingExpiryNotificationSent = false;
        contentHash = computeHash();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        contentHash = computeHash();
    }

    private String computeHash() {
        // setScale(2) so 1000 and 1000.00 hash identically (the DB always returns scale 2)
        String normalizedAmount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP).toPlainString() : "null";
        String rawData = name + normalizedAmount + category + startDate + endDate + frequency + status + notes;
        return sha256(rawData);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) hexString.append(String.format("%02x", b));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }
}
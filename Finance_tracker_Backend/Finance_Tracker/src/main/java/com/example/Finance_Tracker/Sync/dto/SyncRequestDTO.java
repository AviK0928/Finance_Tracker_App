package com.example.Finance_Tracker.Sync.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SyncRequestDTO {
    @NotNull(message = "Last sync time cannot be null")
    private LocalDateTime lastSync;
    private boolean manualSync;
}
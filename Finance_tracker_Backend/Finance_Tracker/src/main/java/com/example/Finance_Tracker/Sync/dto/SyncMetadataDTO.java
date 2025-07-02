package com.example.Finance_Tracker.Sync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SyncMetadataDTO {
    private LocalDateTime latestBudgetUpdate;
    private LocalDateTime latestTransactionUpdate;
}
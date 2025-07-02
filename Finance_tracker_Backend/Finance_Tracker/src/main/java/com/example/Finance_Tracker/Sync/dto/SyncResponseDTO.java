package com.example.Finance_Tracker.Sync.dto;

import com.example.Finance_Tracker.Settings.dto.UserSettingDTO;
import lombok.Data;
import java.util.List;


@Data
public class SyncResponseDTO {
    private List<BudgetDTO> budgets;
    private List<TransactionDTO> transactions;
    private SyncMetadataDTO metadata;
    private List<UserSettingDTO> settings;
    private boolean largeSync;
}
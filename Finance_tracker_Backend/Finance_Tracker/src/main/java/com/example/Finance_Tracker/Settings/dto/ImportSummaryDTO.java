package com.example.Finance_Tracker.Settings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ImportSummaryDTO {
    private int budgetsImported;
    private int budgetsSkipped;
    private int transactionsImported;
    private int transactionsSkipped;
    private int settingsImported;
    private int settingsSkipped;
}
package com.example.Finance_Tracker.Dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private SummaryInfo summary;
    private BudgetInfo budget;
    private TransactionInfo transactions;
}
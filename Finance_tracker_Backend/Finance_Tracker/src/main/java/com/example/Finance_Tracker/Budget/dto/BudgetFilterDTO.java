package com.example.Finance_Tracker.Budget.dto;

import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetFilterDTO {

    @NotNull(message = "Budget status is required")
    private BudgetStatus status;

    @NotNull(message = "Budget frequency is required")
    private BudgetFrequency frequency;

    // userId intentionally excluded (fetched securely via SecurityUtils)
}
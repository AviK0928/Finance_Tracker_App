package com.example.Finance_Tracker.Dashboard.dto;

import com.example.Finance_Tracker.Budget.dto.BudgetResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetInfo {
    // No aggregate total/remaining: budgets can overlap (an all-categories budget and a Food budget both
    // count the same expense), so a sum would subtract one expense several times. Each budget carries its own.
    private List<BudgetResponseDTO> activeBudgets;
}

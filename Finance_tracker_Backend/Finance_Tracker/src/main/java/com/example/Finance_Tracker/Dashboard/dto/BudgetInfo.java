package com.example.Finance_Tracker.Dashboard.dto;

import com.example.Finance_Tracker.Budget.dto.BudgetResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetInfo {
    private BigDecimal totalBudget;
    private BigDecimal remainingBudget;
    private List<BudgetResponseDTO> activeBudgets;
}

package com.example.Finance_Tracker.Sync.mapper;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Sync.dto.BudgetDTO;

public class BudgetMapper {
    public static BudgetDTO toDTO(Budget budget) {
        BudgetDTO dto = new BudgetDTO();
        dto.setAmount(budget.getAmount());
        dto.setPeriod(budget.getFrequency().name());
        dto.setUpdatedAt(budget.getUpdatedAt());
        dto.setContentHash(budget.getContentHash());
        return dto;
    }
}
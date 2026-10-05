package com.example.Finance_Tracker.Budget.dto;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponseDTO {

    private Long id;
    private Long userId;
    private String name;
    private String category;
    private BigDecimal amount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentageSpent;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private BudgetFrequency budgetFrequency;
    private BudgetStatus budgetStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** @param spent amount spent so far, computed by {@code BudgetSpendingCalculator} */
    public static BudgetResponseDTO fromEntity(Budget budget, BigDecimal spent) {
        BigDecimal amount = budget.getAmount();
        BigDecimal remaining = amount.subtract(spent);

        double percent = 0.0;
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            percent = spent.divide(amount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return BudgetResponseDTO.builder()
                .id(budget.getId())
                .userId(budget.getUserId()) // ✅ Corrected
                .name(budget.getName())
                .category(budget.getCategory())
                .amount(amount)
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentageSpent(percent)
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .notes(budget.getNotes())
                .budgetFrequency(budget.getFrequency())
                .budgetStatus(budget.getStatus())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
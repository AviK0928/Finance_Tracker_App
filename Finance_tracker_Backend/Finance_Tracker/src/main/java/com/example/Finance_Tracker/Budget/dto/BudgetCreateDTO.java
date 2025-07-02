package com.example.Finance_Tracker.Budget.dto;

import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetCreateDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Budget frequency is required")
    private BudgetFrequency frequency = BudgetFrequency.NONE;

    private String notes;

    // Optional future support
    // private Long categoryId;
}
package com.example.Finance_Tracker.Budget.dto;

import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUpdateDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    /** Optional. Leave empty for a budget that covers all expense categories. */
    @Size(max = 255, message = "Category must be at most 255 characters")
    private String category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Budget frequency is required")
    private BudgetFrequency frequency;

    private String notes;

    @NotNull(message = "Budget status is required")
    private BudgetStatus status;

    @JsonIgnore
    @AssertTrue(message = "End date must be on or after start date")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}

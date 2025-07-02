package com.example.Finance_Tracker.Report.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDTO {

    @NotNull(message = "Enter a Valid Year")
    private int year;

    @NotNull(message = "Enter a Valid Month")
    private Month month;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    private BigDecimal totalIncome;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    private BigDecimal totalExpense;

    @NotNull
    private BigDecimal netSavings;

    @NotEmpty
    private Map<String, BigDecimal> categoryBreakdown;
}

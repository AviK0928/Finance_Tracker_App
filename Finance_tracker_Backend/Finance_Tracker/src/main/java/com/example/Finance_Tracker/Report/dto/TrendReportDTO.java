package com.example.Finance_Tracker.Report.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendReportDTO {

    @NotNull(message = "Enter a Valid Date")
    private LocalDateTime date;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    private BigDecimal income;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    private BigDecimal expense;
}
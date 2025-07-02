package com.example.Finance_Tracker.Report.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryReportDTO {

    @NotBlank(message = "Category Cannot be Blank")
    private String category;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    private BigDecimal totalSpent;
}
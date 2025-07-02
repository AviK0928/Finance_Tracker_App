package com.example.Finance_Tracker.Sync.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BudgetDTO {

    @NotNull
    @DecimalMin(value = "0.01", message = "Entered Amount Must be Greater Than 0")
    private BigDecimal amount;

    @NotBlank(message = "Period Cannot be Blank")
    private String period;

    @NotNull(message = "Updated At Cannot be Blank")
    private LocalDateTime updatedAt;

    @NotBlank
    private String contentHash;
}
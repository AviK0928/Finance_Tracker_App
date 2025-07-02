package com.example.Finance_Tracker.Sync.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {

    @NotNull
    @DecimalMin(value = "0.01", message = "Entered Amount Must be Greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Updated at Cannot be Blank")
    private LocalDateTime updatedAt;

    private String description;

    @NotNull
    private String contentHash;
}
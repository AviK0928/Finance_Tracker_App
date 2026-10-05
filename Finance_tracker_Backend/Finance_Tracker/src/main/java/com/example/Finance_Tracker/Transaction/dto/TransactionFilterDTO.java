package com.example.Finance_Tracker.Transaction.dto;

import com.example.Finance_Tracker.Transaction.util.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterDTO {

    // For internal use only – injected in controller
    private Long userId;

    private String category;

    private TransactionType type;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    private BigDecimal minAmount;

    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    private BigDecimal maxAmount;
}

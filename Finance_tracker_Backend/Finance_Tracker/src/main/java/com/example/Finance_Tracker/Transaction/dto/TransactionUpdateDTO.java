package com.example.Finance_Tracker.Transaction.dto;

import com.example.Finance_Tracker.Transaction.util.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionUpdateDTO {

    // ✅ Internal use only, set from controller
    private Long userId;

    @NotBlank(message = "Category Cannot be Blank")
    private String category;

    @NotNull(message = "Income/Expense")
    private TransactionType type;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    private Double amount;

    @NotNull(message = "Transaction Date Cannot be Blank")
    private LocalDateTime transactionDate;

    private String description;
}

package com.example.Finance_Tracker.Transaction.dto;

import com.example.Finance_Tracker.Transaction.util.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateDTO {

    // ✅ Removed userId from client input

    @NotBlank(message = "Category Cannot be Blank")
    private String category;

    @NotNull(message = "Income/Expense")
    private TransactionType type;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount Must be Greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Transaction Date Cannot be Blank")
    private LocalDateTime transactionDate;

    private String description;

    // ✅ Add setter for internal use (SecurityUtils)
    private Long userId;
}

package com.example.Finance_Tracker.Transaction.dto;

import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {

    private Long id;
    private Long userId;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;

    public static TransactionResponseDTO fromEntity(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}

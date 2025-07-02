package com.example.Finance_Tracker.Sync.mapper;

import com.example.Finance_Tracker.Sync.dto.TransactionDTO;
import com.example.Finance_Tracker.Transaction.entity.Transaction;

import java.math.BigDecimal;

public class TransactionMapper {
    public static TransactionDTO toDTO(Transaction entity) {
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(BigDecimal.valueOf(entity.getAmount()));
        dto.setDescription(entity.getDescription());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setContentHash(entity.getContentHash());
        return dto;
    }
}
package com.example.Finance_Tracker.Sync.mapper;

import com.example.Finance_Tracker.Sync.dto.TransactionDTO;
import com.example.Finance_Tracker.Transaction.entity.Transaction;

public class TransactionMapper {
    public static TransactionDTO toDTO(Transaction entity) {
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(entity.getAmount());
        dto.setDescription(entity.getDescription());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setContentHash(entity.getContentHash());
        return dto;
    }
}
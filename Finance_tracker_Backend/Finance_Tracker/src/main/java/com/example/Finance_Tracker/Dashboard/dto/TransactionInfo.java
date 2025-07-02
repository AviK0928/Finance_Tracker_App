package com.example.Finance_Tracker.Dashboard.dto;

import com.example.Finance_Tracker.Transaction.dto.TransactionResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInfo {
    private List<TransactionResponseDTO> recentTransactions;
}

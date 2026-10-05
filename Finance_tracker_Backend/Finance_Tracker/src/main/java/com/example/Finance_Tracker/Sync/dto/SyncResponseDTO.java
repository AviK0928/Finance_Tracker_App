package com.example.Finance_Tracker.Sync.dto;

import com.example.Finance_Tracker.Budget.dto.BudgetResponseDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionResponseDTO;

import java.util.List;

/**
 * Response of {@code GET /api/sync}.
 *
 * @param cursor                opaque position; the app sends it back unchanged on the next sync
 * @param fullSync              true when no cursor was sent: the app replaces its local transactions
 * @param transactions          transactions created or changed since the cursor (all of them on a full sync)
 * @param deletedTransactionIds transactions deleted since the cursor (empty on a full sync)
 * @param budgets               always the complete budget list, spending computed now
 */
public record SyncResponseDTO(
        String cursor,
        boolean fullSync,
        List<TransactionResponseDTO> transactions,
        List<Long> deletedTransactionIds,
        List<BudgetResponseDTO> budgets
) {
}

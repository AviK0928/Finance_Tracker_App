package com.example.Finance_Tracker.Sync.service;

import com.example.Finance_Tracker.Budget.service.BudgetService;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Sync.dto.SyncResponseDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionResponseDTO;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.DeletedTransactionRepository;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Read-only sync for the app's offline cache.
 * <ul>
 *   <li>Transactions: changes since the cursor plus the ids deleted since then; everything without a cursor.</li>
 *   <li>Budgets: always all of them. Their spending changes whenever a transaction changes, without the
 *       budget row changing, so "budgets changed since X" would leave stale numbers on the phone.</li>
 * </ul>
 * The cursor is the server time taken before the reads, so the phone's clock never matters.
 */
@Service
@RequiredArgsConstructor
public class SyncService {

    /**
     * Rows changed up to this long before the cursor are sent again. Covers a write whose timestamp was
     * taken before the previous sync read but which committed after it. Re-sent rows are harmless:
     * the app stores by id.
     */
    static final Duration OVERLAP = Duration.ofMinutes(2);

    private final TransactionRepository transactionRepository;
    private final DeletedTransactionRepository deletedTransactionRepository;
    private final BudgetService budgetService;

    public SyncResponseDTO sync(String cursor) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        LocalDateTime now = LocalDateTime.now();
        boolean fullSync = cursor == null || cursor.isBlank();

        List<Transaction> transactions;
        List<Long> deletedIds;
        if (fullSync) {
            transactions = transactionRepository.findByUserId(userId);
            deletedIds = List.of();
        } else {
            LocalDateTime since = parseCursor(cursor, now).minus(OVERLAP);
            transactions = transactionRepository.findByUserIdAndUpdatedAtAfter(userId, since);
            deletedIds = deletedTransactionRepository.findTransactionIdsDeletedSince(userId, since);
        }

        return new SyncResponseDTO(
                now.toString(),
                fullSync,
                transactions.stream().map(TransactionResponseDTO::fromEntity).toList(),
                deletedIds,
                budgetService.getBudgetsByUser());
    }

    /** A cursor is only ever one this server returned: a past local date-time. */
    private static LocalDateTime parseCursor(String cursor, LocalDateTime now) {
        LocalDateTime parsed;
        try {
            parsed = LocalDateTime.parse(cursor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid sync cursor");
        }
        if (parsed.isAfter(now)) {
            throw new IllegalArgumentException("Invalid sync cursor");
        }
        return parsed;
    }
}

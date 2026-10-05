package com.example.Finance_Tracker.Sync.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import com.example.Finance_Tracker.Notification.util.NotificationType;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import com.example.Finance_Tracker.Settings.dto.UserSettingDTO;
import com.example.Finance_Tracker.Settings.service.UserSettingService;
import com.example.Finance_Tracker.Sync.dto.*;
import com.example.Finance_Tracker.Sync.mapper.BudgetMapper;
import com.example.Finance_Tracker.Sync.mapper.TransactionMapper;
import com.example.Finance_Tracker.Sync.util.HashUtils;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserSettingService userSettingService; // Must implement getAllSettingsForUser(userId)

    private static final int LARGE_SYNC_THRESHOLD = 100;

    public SyncResponseDTO syncData(Long userId, SyncRequestDTO request) {
        LocalDateTime lastSync = request.getLastSync();

        if (lastSync == null || lastSync.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid sync timestamp.");
        }

        if (request.isManualSync()) {
            CreateNotificationDTO start = new CreateNotificationDTO();
            start.setTitle("Sync Started");
            start.setMessage("Your finance data sync has started.");
            start.setType(NotificationType.INFO);
            start.setPreference(SettingKey.NOTIFY_SYNC_EVENTS);
            notificationService.createNotificationForUser(userId, start);
        }

        List<Budget> recentBudgets = budgetRepository.findAllByUserIdAndUpdatedAtAfter(userId, lastSync);
        List<Transaction> recentTransactions = transactionRepository.findAllByUserIdAndUpdatedAtAfter(userId, lastSync);

        List<BudgetDTO> updatedBudgets = recentBudgets.stream()
                .map(budget -> {
                    BudgetDTO dto = BudgetMapper.toDTO(budget);
                    dto.setContentHash(HashUtils.computeBudgetHash(budget));
                    return dto;
                })
                .collect(Collectors.toList());

        List<TransactionDTO> updatedTransactions = recentTransactions.stream()
                .map(transaction -> {
                    TransactionDTO dto = TransactionMapper.toDTO(transaction);
                    dto.setContentHash(HashUtils.computeTransactionHash(transaction));
                    return dto;
                })
                .collect(Collectors.toList());

        SyncMetadataDTO metadata = getSyncMetadata(userId);
        List<UserSettingDTO> settings = userSettingService.getAllSettingsForUser(userId); // provide userId version

        SyncResponseDTO response = new SyncResponseDTO();
        response.setBudgets(updatedBudgets);
        response.setTransactions(updatedTransactions);
        response.setMetadata(metadata);
        response.setSettings(settings);
        response.setLargeSync(updatedBudgets.size() + updatedTransactions.size() > LARGE_SYNC_THRESHOLD);

        if (request.isManualSync()) {
            CreateNotificationDTO done = new CreateNotificationDTO();
            done.setTitle("Sync Complete");
            done.setMessage("Your data has been synced successfully.");
            done.setType(NotificationType.SYNC_SUCCESS);
            done.setPreference(SettingKey.NOTIFY_SYNC_EVENTS);
            notificationService.createNotificationForUser(userId, done);
        }

        return response;
    }

    public SyncMetadataDTO getSyncMetadata(Long userId) {
        LocalDateTime latestBudgetUpdate = budgetRepository.findLatestUpdateForUser(userId);
        LocalDateTime latestTransactionUpdate = transactionRepository.findLatestUpdateForUser(userId);

        return new SyncMetadataDTO(latestBudgetUpdate, latestTransactionUpdate);
    }
}
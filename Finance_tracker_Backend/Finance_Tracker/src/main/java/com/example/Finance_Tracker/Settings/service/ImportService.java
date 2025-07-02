package com.example.Finance_Tracker.Settings.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Settings.dto.ImportSummaryDTO;
import com.example.Finance_Tracker.Settings.entity.UserSetting;
import com.example.Finance_Tracker.Settings.repository.UserSettingRepository;
import com.example.Finance_Tracker.Settings.util.CSVImportUtil;
import com.example.Finance_Tracker.Settings.util.ZipUtil;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserSettingRepository userSettingRepository;

    private static final String BUDGETS_CSV = "budgets.csv";
    private static final String TRANSACTIONS_CSV = "transactions.csv";
    private static final String SETTINGS_CSV = "settings.csv";

    @Transactional
    public ImportSummaryDTO importUserData(MultipartFile file, Long userId) {
        logger.info("Starting import of user data for userId {}", userId);

        Map<String, InputStream> extractedFiles = ZipUtil.extractCsvFiles(file,
                List.of(BUDGETS_CSV, TRANSACTIONS_CSV, SETTINGS_CSV));

        List<Budget> budgets = CSVImportUtil.parseBudgetsCSV(extractedFiles.get(BUDGETS_CSV));
        List<Transaction> transactions = CSVImportUtil.parseTransactionsCSV(extractedFiles.get(TRANSACTIONS_CSV));
        List<UserSetting> settings = CSVImportUtil.parseSettingsCSV(extractedFiles.get(SETTINGS_CSV));

        boolean invalidBudget = budgets.stream().anyMatch(b -> !b.getUserId().equals(userId));
        boolean invalidTxn = transactions.stream().anyMatch(t -> !t.getUserId().equals(userId));
        boolean invalidSetting = settings.stream().anyMatch(s -> !s.getUserId().equals(userId));

        if (invalidBudget || invalidTxn || invalidSetting) {
            throw new IllegalArgumentException("Import failed: Detected records not belonging to the authenticated user.");
        }

        return importUserData(budgets, transactions, settings);
    }

    /**
     * Internal method to persist parsed data with deduplication.
     */
    private ImportSummaryDTO importUserData(List<Budget> budgets,
                                            List<Transaction> transactions,
                                            List<UserSetting> settings) {

        int budgetsImported = 0, budgetsSkipped = 0;
        for (Budget b : budgets) {
            if (!budgetRepository.existsByUserIdAndContentHash(b.getUserId(), b.getContentHash())) {
                budgetRepository.save(b);
                budgetsImported++;
            } else {
                logger.info("Skipped duplicate budget with hash {}", b.getContentHash());
                budgetsSkipped++;
            }
        }

        int transactionsImported = 0, transactionsSkipped = 0;
        for (Transaction t : transactions) {
            if (!transactionRepository.existsByUserIdAndContentHash(t.getUserId(), t.getContentHash())) {
                transactionRepository.save(t);
                transactionsImported++;
            } else {
                logger.info("Skipped duplicate transaction with hash {}", t.getContentHash());
                transactionsSkipped++;
            }
        }

        int settingsImported = 0, settingsSkipped = 0;
        for (UserSetting s : settings) {
            if (!userSettingRepository.existsByUserIdAndKey(s.getUserId(), s.getKey())) {
                userSettingRepository.save(s);
                settingsImported++;
            } else {
                logger.info("Skipped existing setting {} for user {}", s.getKey(), s.getUserId());
                settingsSkipped++;
            }
        }

        return ImportSummaryDTO.builder()
                .budgetsImported(budgetsImported)
                .budgetsSkipped(budgetsSkipped)
                .transactionsImported(transactionsImported)
                .transactionsSkipped(transactionsSkipped)
                .settingsImported(settingsImported)
                .settingsSkipped(settingsSkipped)
                .build();
    }
}
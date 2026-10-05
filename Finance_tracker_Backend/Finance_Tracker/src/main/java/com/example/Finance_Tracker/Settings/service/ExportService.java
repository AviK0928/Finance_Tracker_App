package com.example.Finance_Tracker.Settings.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Settings.entity.UserSetting;
import com.example.Finance_Tracker.Settings.repository.UserSettingRepository;
import com.example.Finance_Tracker.Settings.util.CSVExportUtil;
import com.example.Finance_Tracker.Settings.util.ZipUtil;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserSettingRepository settingRepository;

    /**
     * Generates a ZIP file with:
     * - budgets.csv
     * - transactions.csv
     * - settings.csv
     * - transaction_report.pdf
     */
    public byte[] exportUserData(Long userId) {
        List<Budget> budgets = budgetRepository.findAllByUserId(userId);
        List<Transaction> transactions = transactionRepository.findAllByUserId(userId);
        List<UserSetting> settings = settingRepository.findByUserId(userId);

        Map<String, byte[]> files = new HashMap<>();
        files.put(ImportService.BUDGETS_CSV, CSVExportUtil.exportBudgetsToCSV(budgets));
        files.put(ImportService.TRANSACTIONS_CSV, CSVExportUtil.exportTransactionsToCSV(transactions));
        files.put(ImportService.SETTINGS_CSV, CSVExportUtil.exportSettingsToCSV(settings));
        files.put("transaction_report.pdf", PDFGenerator.generateTransactionPDF(transactions));

        return ZipUtil.createZipFromFiles(files);
    }

    public String generateExportFilename() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "finance_export_" + timestamp + ".zip";
    }
}
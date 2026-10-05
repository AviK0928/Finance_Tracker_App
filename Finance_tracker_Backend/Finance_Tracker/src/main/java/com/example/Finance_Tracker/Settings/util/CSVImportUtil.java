package com.example.Finance_Tracker.Settings.util;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Budget.util.BudgetUsageAlertStage;
import com.example.Finance_Tracker.Settings.entity.UserSetting;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CSVImportUtil {

    public static List<Budget> parseBudgetsCSV(InputStream inputStream) {
        List<Budget> budgets = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(inputStream))) {

            for (CSVRecord record : parser) {
                Budget budget = Budget.builder()
                        .userId(Long.parseLong(record.get("userId")))
                        .name(record.get("name"))
                        .amount(new BigDecimal(record.get("amount")))
                        .spentAmount(new BigDecimal(record.get("spentAmount")))
                        .startDate(LocalDate.parse(record.get("startDate")))
                        .endDate(LocalDate.parse(record.get("endDate")))
                        .createdAt(LocalDateTime.parse(record.get("createdAt")))
                        .updatedAt(LocalDateTime.parse(record.get("updatedAt")))
                        .frequency(BudgetFrequency.valueOf(record.get("frequency")))
                        .status(BudgetStatus.valueOf(record.get("status")))
                        .notes(record.get("notes").isEmpty() ? null : record.get("notes"))
                        .lastNotifiedStage(BudgetUsageAlertStage.valueOf(record.get("lastNotifiedStage")))
                        .expiryNotificationSent(Boolean.parseBoolean(record.get("expiryNotificationSent")))
                        .nearingExpiryNotificationSent(Boolean.parseBoolean(record.get("nearingExpiryNotificationSent")))
                        .contentHash(record.get("contentHash"))
                        .build();
                budgets.add(budget);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing budgets CSV", e);
        }
        return budgets;
    }

    public static List<Transaction> parseTransactionsCSV(InputStream inputStream) {
        List<Transaction> transactions = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(inputStream))) {

            for (CSVRecord record : parser) {
                Transaction txn = new Transaction();
                txn.setUserId(Long.parseLong(record.get("userId")));
                txn.setAmount(new BigDecimal(record.get("amount")));
                txn.setType(TransactionType.valueOf(record.get("type")));
                txn.setCategory(record.get("category"));
                txn.setDescription(record.get("description").isEmpty() ? null : record.get("description"));
                txn.setTransactionDate(LocalDateTime.parse(record.get("transactionDate")));
                txn.setCreatedAt(LocalDateTime.parse(record.get("createdAt")));
                txn.setUpdatedAt(LocalDateTime.parse(record.get("updatedAt")));
                txn.setContentHash(record.get("contentHash"));
                transactions.add(txn);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing transactions CSV", e);
        }
        return transactions;
    }

    public static List<UserSetting> parseSettingsCSV(InputStream inputStream) {
        List<UserSetting> settings = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(inputStream))) {

            for (CSVRecord record : parser) {
                UserSetting setting = UserSetting.builder()
                        .userId(Long.parseLong(record.get("userId")))
                        .key(SettingKey.valueOf(record.get("key")))
                        .value(record.get("value"))
                        .updatedAt(LocalDateTime.parse(record.get("updatedAt")))
                        .build();
                settings.add(setting);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing settings CSV", e);
        }
        return settings;
    }
}
package com.example.Finance_Tracker.Report.service;

import com.example.Finance_Tracker.Report.dto.CategoryReportDTO;
import com.example.Finance_Tracker.Report.dto.MonthlyReportDTO;
import com.example.Finance_Tracker.Report.dto.TrendReportDTO;
import com.example.Finance_Tracker.Report.model.Report;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    public MonthlyReportDTO getMonthlyReport(Month month, int year) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59, 999_999_999);

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDateTime, endDateTime);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Transaction txn : transactions) {
            BigDecimal amt = BigDecimal.valueOf(txn.getAmount());
            if ("income".equalsIgnoreCase(String.valueOf(txn.getType()))) {
                totalIncome = totalIncome.add(amt);
            } else {
                totalExpense = totalExpense.add(amt);
                categoryBreakdown.merge(txn.getCategory(), amt, BigDecimal::add);
            }
        }

        return new MonthlyReportDTO(year, month, totalIncome, totalExpense, totalIncome.subtract(totalExpense), categoryBreakdown);
    }

    public List<CategoryReportDTO> getCategoryWiseReport() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        for (Transaction txn : transactions) {
            if ("expense".equalsIgnoreCase(String.valueOf(txn.getType()))) {
                categoryTotals.merge(txn.getCategory(), BigDecimal.valueOf(txn.getAmount()), BigDecimal::add);
            }
        }

        return categoryTotals.entrySet().stream()
                .map(entry -> new CategoryReportDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<TrendReportDTO> getTrendReport(String period) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        LocalDate end = LocalDate.now();
        LocalDate start = switch (period) {
            case "1m" -> end.minusMonths(1);
            case "3m" -> end.minusMonths(3);
            case "6m" -> end.minusMonths(6);
            case "1y" -> end.minusYears(1);
            default -> end.minusMonths(6);
        };

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59, 999_999_999);

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDateTime, endDateTime);

        Map<LocalDate, Report> dailyMap = new TreeMap<>();

        for (Transaction txn : transactions) {
            LocalDate date = txn.getTransactionDate().toLocalDate();
            Report data = dailyMap.getOrDefault(date, new Report(date.atStartOfDay(), BigDecimal.ZERO, BigDecimal.ZERO));
            if ("income".equalsIgnoreCase(String.valueOf(txn.getType()))) {
                data.setIncome(data.getIncome().add(BigDecimal.valueOf(txn.getAmount())));
            } else {
                data.setExpense(data.getExpense().add(BigDecimal.valueOf(txn.getAmount())));
            }
            dailyMap.put(date, data);
        }

        return dailyMap.values().stream()
                .map(data -> new TrendReportDTO(data.getDate(), data.getIncome(), data.getExpense()))
                .collect(Collectors.toList());
    }
}

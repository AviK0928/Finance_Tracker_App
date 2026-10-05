package com.example.Finance_Tracker.Report;

import com.example.Finance_Tracker.Report.dto.CategoryReportDTO;
import com.example.Finance_Tracker.Report.dto.MonthlyReportDTO;
import com.example.Finance_Tracker.Report.dto.TrendReportDTO;
import com.example.Finance_Tracker.Report.service.ReportService;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import com.example.Finance_Tracker.User.mapper.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final Long USER_ID = 7L;

    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private ReportService reportService;

    @BeforeEach
    void login() {
        CustomUserDetails principal = new CustomUserDetails(USER_ID, "me@example.com", "hash", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void monthlyReport_mergesCategoriesThatDifferOnlyInCase() {
        LocalDateTime day = LocalDateTime.of(2026, 10, 3, 12, 0);
        when(transactionRepository.findByUserIdAndTransactionDateBetween(
                USER_ID, LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 31, 23, 59, 59, 999_999_999)))
                .thenReturn(List.of(
                        txn(TransactionType.EXPENSE, "Food", "100.00", day),
                        txn(TransactionType.EXPENSE, "food", "50.00", day),
                        txn(TransactionType.EXPENSE, "Rent", "300.00", day),
                        txn(TransactionType.INCOME, "Salary", "1000.00", day)));

        MonthlyReportDTO report = reportService.getMonthlyReport(Month.OCTOBER, 2026);

        assertThat(report.getCategoryBreakdown()).containsOnlyKeys("Food", "Rent");
        assertThat(report.getCategoryBreakdown().get("Food")).isEqualByComparingTo("150.00");
        assertThat(report.getTotalIncome()).isEqualByComparingTo("1000.00");
        assertThat(report.getTotalExpense()).isEqualByComparingTo("450.00");
        assertThat(report.getNetSavings()).isEqualByComparingTo("550.00");
    }

    @Test
    void categoryReport_mergesCaseInsensitively_ignoresIncome_andIsSortedByName() {
        LocalDateTime day = LocalDateTime.of(2026, 10, 3, 12, 0);
        when(transactionRepository.findByUserId(USER_ID)).thenReturn(List.of(
                txn(TransactionType.EXPENSE, "rent", "300.00", day),
                txn(TransactionType.EXPENSE, "Food", "100.00", day),
                txn(TransactionType.EXPENSE, "FOOD", "25.50", day),
                txn(TransactionType.INCOME, "Salary", "1000.00", day)));

        List<CategoryReportDTO> report = reportService.getCategoryWiseReport();

        assertThat(report).extracting(CategoryReportDTO::getCategory).containsExactly("Food", "rent");
        assertThat(report.get(0).getTotalSpent()).isEqualByComparingTo("125.50");
        assertThat(report.get(1).getTotalSpent()).isEqualByComparingTo("300.00");
    }

    @Test
    void trendReport_sumsPerDay_inDateOrder() {
        LocalDate today = LocalDate.now();
        when(transactionRepository.findByUserIdAndTransactionDateBetween(eq(USER_ID), any(), any())).thenReturn(List.of(
                txn(TransactionType.EXPENSE, "Food", "40.00", today.atTime(18, 0)),
                txn(TransactionType.INCOME, "Salary", "500.00", today.minusDays(2).atTime(9, 0)),
                txn(TransactionType.EXPENSE, "Food", "10.00", today.atTime(8, 0))));

        List<TrendReportDTO> trend = reportService.getTrendReport("1m");

        assertThat(trend).extracting(TrendReportDTO::getDate)
                .containsExactly(today.minusDays(2).atStartOfDay(), today.atStartOfDay());
        assertThat(trend.get(0).getIncome()).isEqualByComparingTo("500.00");
        assertThat(trend.get(1).getExpense()).isEqualByComparingTo("50.00");
        assertThat(trend.get(1).getIncome()).isEqualByComparingTo("0");
    }

    private static Transaction txn(TransactionType type, String category, String amount, LocalDateTime date) {
        Transaction t = new Transaction();
        t.setUserId(USER_ID);
        t.setType(type);
        t.setCategory(category);
        t.setAmount(new BigDecimal(amount));
        t.setTransactionDate(date);
        return t;
    }
}

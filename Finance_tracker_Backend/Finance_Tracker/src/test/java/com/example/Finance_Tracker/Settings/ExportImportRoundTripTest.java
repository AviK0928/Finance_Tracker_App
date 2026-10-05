package com.example.Finance_Tracker.Settings;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Budget.repository.BudgetRepository;
import com.example.Finance_Tracker.Budget.util.BudgetFrequency;
import com.example.Finance_Tracker.Budget.util.BudgetStatus;
import com.example.Finance_Tracker.Budget.util.BudgetUsageAlertStage;
import com.example.Finance_Tracker.Settings.dto.ImportSummaryDTO;
import com.example.Finance_Tracker.Settings.entity.UserSetting;
import com.example.Finance_Tracker.Settings.repository.UserSettingRepository;
import com.example.Finance_Tracker.Settings.service.ExportService;
import com.example.Finance_Tracker.Settings.service.ImportService;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import com.example.Finance_Tracker.Settings.util.ZipUtil;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** An export ZIP must be importable by the same user, and bad uploads must fail as 400-type errors. */
@ExtendWith(MockitoExtension.class)
class ExportImportRoundTripTest {

    private static final long USER = 7L;
    private static final LocalDateTime AT = LocalDateTime.of(2026, 10, 5, 10, 0);

    @Mock private BudgetRepository budgetRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserSettingRepository settingRepository;
    @InjectMocks private ExportService exportService;

    private ImportService importService;

    @BeforeEach
    void setUp() {
        importService = new ImportService(budgetRepository, transactionRepository, settingRepository);
    }

    @Test
    void exportedZip_isImportable_andSummaryCountsEachRow() {
        byte[] zip = exportOneOfEach();
        when(budgetRepository.existsByUserIdAndContentHash(eq(USER), any())).thenReturn(false);
        when(transactionRepository.existsByUserIdAndContentHash(eq(USER), any())).thenReturn(false);
        when(settingRepository.existsByUserIdAndKey(USER, SettingKey.DEFAULT_CURRENCY)).thenReturn(false);

        ImportSummaryDTO summary = importService.importUserData(upload(zip), USER);

        assertEquals(1, summary.getBudgetsImported());
        assertEquals(1, summary.getTransactionsImported());
        assertEquals(1, summary.getSettingsImported());
        assertEquals(0, summary.getBudgetsSkipped() + summary.getTransactionsSkipped() + summary.getSettingsSkipped());

        ArgumentCaptor<Budget> budget = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(budget.capture());
        assertEquals("Groceries", budget.getValue().getName());
        assertNull(budget.getValue().getId(), "ids from the file must not be reused");

        ArgumentCaptor<Transaction> txn = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txn.capture());
        assertEquals(0, new BigDecimal("250.75").compareTo(txn.getValue().getAmount()));

        ArgumentCaptor<UserSetting> setting = ArgumentCaptor.forClass(UserSetting.class);
        verify(settingRepository).save(setting.capture());
        assertEquals("USD", setting.getValue().getValue());
    }

    @Test
    void importingAnotherUsersExport_isRejected_andNothingIsSaved() {
        byte[] zip = exportOneOfEach();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> importService.importUserData(upload(zip), 8L));

        assertTrue(ex.getMessage().contains("not belonging"));
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void malformedCsv_isIllegalArgument_notAServerError() {
        byte[] zip = ZipUtil.createZipFromFiles(Map.of(
                "budgets.csv", "not,the,right,header\n1,2,3,4\n".getBytes(StandardCharsets.UTF_8),
                "transactions.csv", "id\n".getBytes(StandardCharsets.UTF_8),
                "settings.csv", "id\n".getBytes(StandardCharsets.UTF_8)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> importService.importUserData(upload(zip), USER));

        assertTrue(ex.getMessage().startsWith("Invalid budgets.csv"), ex.getMessage());
    }

    @Test
    void oversizedEntry_isRejectedBeforeParsing() {
        byte[] zip = ZipUtil.createZipFromFiles(Map.of(
                "budgets.csv", new byte[(int) ZipUtil.MAX_ENTRY_BYTES + 1],
                "transactions.csv", new byte[0],
                "settings.csv", new byte[0]));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> importService.importUserData(upload(zip), USER));

        assertTrue(ex.getMessage().contains("too large"), ex.getMessage());
    }

    private byte[] exportOneOfEach() {
        Budget budget = Budget.builder()
                .id(11L).userId(USER).name("Groceries").category("Food").amount(new BigDecimal("5000.00"))
                .startDate(LocalDate.of(2026, 10, 1)).endDate(LocalDate.of(2026, 10, 31))
                .createdAt(AT).updatedAt(AT)
                .frequency(BudgetFrequency.MONTHLY).status(BudgetStatus.ACTIVE)
                .lastNotifiedStage(BudgetUsageAlertStage.NONE).contentHash("b-hash")
                .build();

        Transaction txn = new Transaction();
        txn.setId(21L);
        txn.setUserId(USER);
        txn.setAmount(new BigDecimal("250.75"));
        txn.setType(TransactionType.EXPENSE);
        txn.setCategory("Food");
        txn.setDescription("Lunch");
        txn.setTransactionDate(AT);
        txn.setCreatedAt(AT);
        txn.setUpdatedAt(AT);
        txn.setContentHash("t-hash");

        UserSetting setting = UserSetting.builder()
                .id(31L).userId(USER).key(SettingKey.DEFAULT_CURRENCY).value("USD").updatedAt(AT)
                .build();

        when(budgetRepository.findAllByUserId(USER)).thenReturn(List.of(budget));
        when(transactionRepository.findAllByUserId(USER)).thenReturn(List.of(txn));
        when(settingRepository.findByUserId(USER)).thenReturn(List.of(setting));

        return exportService.exportUserData(USER);
    }

    private static MockMultipartFile upload(byte[] zip) {
        return new MockMultipartFile("file", "finance_export.zip", "application/zip", zip);
    }
}

package com.example.Finance_Tracker.Transaction;

import com.example.Finance_Tracker.Budget.service.BudgetAlertService;
import com.example.Finance_Tracker.Notification.dto.CreateNotificationDTO;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import com.example.Finance_Tracker.Transaction.dto.TransactionCreateDTO;
import com.example.Finance_Tracker.Transaction.dto.TransactionUpdateDTO;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.TransactionRepository;
import com.example.Finance_Tracker.Transaction.service.TransactionService;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.mapper.CustomUserDetails;
import com.example.Finance_Tracker.User.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Per-transaction spending alerts: one expense produces at most one of them. */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final long USER_ID = 5L;

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private BudgetAlertService budgetAlertService;
    @InjectMocks private TransactionService transactionService;

    @BeforeEach
    void login() {
        CustomUserDetails principal = new CustomUserDetails(USER_ID, "me@example.com", "hash", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    /** Persistence used by createTransaction. */
    private void stubCreate() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User()));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(99L);
            return t;
        });
        when(transactionRepository.findByUserIdAndTransactionDateBetween(eq(USER_ID), any(), any()))
                .thenReturn(List.of());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void expenseOver10k_sendsOnlyTheHighValueAlert() {
        stubCreate();
        transactionService.createTransaction(expense("12000"));

        CreateNotificationDTO sent = onlyNotification();
        assertThat(sent.getTitle()).isEqualTo("High Value Expense");
        assertThat(sent.getPreference()).isEqualTo(SettingKey.NOTIFY_SPENDING_ALERTS);
    }

    @Test
    void expenseBetween5kAnd10k_sendsTheHeavySpendingAlert() {
        stubCreate();
        transactionService.createTransaction(expense("6000"));

        CreateNotificationDTO sent = onlyNotification();
        assertThat(sent.getTitle()).isEqualTo("Heavy Spending in Food");
        assertThat(sent.getPreference()).isEqualTo(SettingKey.NOTIFY_SPENDING_ALERTS);
    }

    @Test
    void smallExpense_sendsNoSpendingAlert() {
        stubCreate();
        transactionService.createTransaction(expense("3000"));

        verify(notificationService, never()).createNotification(any());
    }

    @Test
    void deletingAnExpense_reevaluatesTheBudgetsItCountedTowards() {
        Transaction existing = existingExpense();
        when(transactionRepository.findById(99L)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(99L);

        verify(transactionRepository).delete(existing);
        verify(budgetAlertService).onExpenseRecorded(USER_ID, "Food", LocalDate.of(2026, 10, 5));
    }

    @Test
    void movingAnExpenseToAnotherCategory_reevaluatesOldAndNewBudgets() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.of(existingExpense()));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        TransactionUpdateDTO update = new TransactionUpdateDTO();
        update.setCategory("Travel");

        transactionService.updateTransaction(99L, update);

        verify(budgetAlertService).onExpenseRecorded(USER_ID, "Food", LocalDate.of(2026, 10, 5));
        verify(budgetAlertService).onExpenseRecorded(USER_ID, "Travel", LocalDate.of(2026, 10, 5));
    }

    private static Transaction existingExpense() {
        Transaction t = new Transaction();
        t.setId(99L);
        t.setUserId(USER_ID);
        t.setAmount(new BigDecimal("600"));
        t.setType(TransactionType.EXPENSE);
        t.setCategory("Food");
        t.setTransactionDate(LocalDateTime.of(2026, 10, 5, 10, 0));
        return t;
    }

    private CreateNotificationDTO onlyNotification() {
        ArgumentCaptor<CreateNotificationDTO> captor = ArgumentCaptor.forClass(CreateNotificationDTO.class);
        verify(notificationService, times(1)).createNotification(captor.capture());
        return captor.getValue();
    }

    private static TransactionCreateDTO expense(String amount) {
        TransactionCreateDTO dto = new TransactionCreateDTO();
        dto.setAmount(new BigDecimal(amount));
        dto.setCategory("Food");
        dto.setType(TransactionType.EXPENSE);
        dto.setTransactionDate(LocalDateTime.of(2026, 10, 5, 10, 0));
        return dto;
    }
}

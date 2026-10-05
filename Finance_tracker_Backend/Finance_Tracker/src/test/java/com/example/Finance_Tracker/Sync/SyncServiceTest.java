package com.example.Finance_Tracker.Sync;

import com.example.Finance_Tracker.Budget.dto.BudgetResponseDTO;
import com.example.Finance_Tracker.Budget.service.BudgetService;
import com.example.Finance_Tracker.Sync.dto.SyncResponseDTO;
import com.example.Finance_Tracker.Sync.service.SyncService;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.repository.DeletedTransactionRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    private static final Long USER_ID = 7L;

    @Mock private TransactionRepository transactionRepository;
    @Mock private DeletedTransactionRepository deletedTransactionRepository;
    @Mock private BudgetService budgetService;
    @InjectMocks private SyncService syncService;

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
    void withoutCursor_sendsEverything_andAServerCursor() {
        LocalDateTime start = LocalDateTime.now();
        when(transactionRepository.findByUserId(USER_ID)).thenReturn(List.of(transaction(1L)));
        BudgetResponseDTO budget = new BudgetResponseDTO();
        when(budgetService.getBudgetsByUser()).thenReturn(List.of(budget));

        SyncResponseDTO response = syncService.sync(null);

        assertThat(response.fullSync()).isTrue();
        assertThat(response.transactions()).extracting("id").containsExactly(1L);
        assertThat(response.deletedTransactionIds()).isEmpty();
        assertThat(response.budgets()).containsExactly(budget);
        assertThat(LocalDateTime.parse(response.cursor())).isAfterOrEqualTo(start);
        verifyNoInteractions(deletedTransactionRepository);
        verify(transactionRepository, never()).findByUserIdAndUpdatedAtAfter(anyLong(), any());
    }

    @Test
    void withCursor_sendsChangesAndDeletionsSinceTheCursorMinusTheOverlap() {
        LocalDateTime cursor = LocalDateTime.of(2026, 10, 5, 10, 0);
        LocalDateTime since = LocalDateTime.of(2026, 10, 5, 9, 58);
        when(transactionRepository.findByUserIdAndUpdatedAtAfter(USER_ID, since)).thenReturn(List.of(transaction(2L)));
        when(deletedTransactionRepository.findTransactionIdsDeletedSince(USER_ID, since)).thenReturn(List.of(3L));
        when(budgetService.getBudgetsByUser()).thenReturn(List.of());

        SyncResponseDTO response = syncService.sync(cursor.toString());

        assertThat(response.fullSync()).isFalse();
        assertThat(response.transactions()).extracting("id").containsExactly(2L);
        assertThat(response.deletedTransactionIds()).containsExactly(3L);
        verify(transactionRepository, never()).findByUserId(any());
    }

    @Test
    void cursorInTheFuture_isRejected() {
        String future = LocalDateTime.now().plusDays(1).toString();

        assertThatThrownBy(() -> syncService.sync(future))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid sync cursor");
        verifyNoInteractions(transactionRepository, deletedTransactionRepository, budgetService);
    }

    @Test
    void malformedCursor_isRejected() {
        assertThatThrownBy(() -> syncService.sync("yesterday"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid sync cursor");
        verifyNoInteractions(transactionRepository, deletedTransactionRepository, budgetService);
    }

    private static Transaction transaction(Long id) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setUserId(USER_ID);
        t.setType(TransactionType.EXPENSE);
        t.setCategory("Food");
        t.setAmount(new BigDecimal("10.00"));
        t.setTransactionDate(LocalDateTime.of(2026, 10, 5, 9, 0));
        return t;
    }
}

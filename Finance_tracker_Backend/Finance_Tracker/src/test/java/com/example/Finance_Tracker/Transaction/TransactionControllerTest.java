package com.example.Finance_Tracker.Transaction;

import com.example.Finance_Tracker.Core.exception.GlobalExceptionHandler;
import com.example.Finance_Tracker.Transaction.controller.TransactionController;
import com.example.Finance_Tracker.Transaction.dto.TransactionFilterDTO;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import com.example.Finance_Tracker.Transaction.service.TransactionService;
import com.example.Finance_Tracker.Transaction.util.TransactionType;
import com.example.Finance_Tracker.User.mapper.CustomUserDetails;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pins the HTTP contract of /api/transactions: status codes, Location header and JSON shape. */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private static final long USER_ID = 7L;

    @Mock private TransactionService transactionService;
    @InjectMocks private TransactionController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                // Resolves Pageable from page/size/sort query parameters, as Spring Boot does at runtime
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                // Same date format as the running app: Spring Boot disables WRITE_DATES_AS_TIMESTAMPS,
                // a standalone MockMvc does not, so dates would otherwise serialize as [2026,10,5,10,0]
                .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()))
                .build();
        CustomUserDetails user = new CustomUserDetails(USER_ID, "gil@example.com", "n/a", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static Transaction transaction(long id) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setUserId(USER_ID);
        t.setAmount(new BigDecimal("1200.50"));
        t.setType(TransactionType.EXPENSE);
        t.setCategory("Food");
        t.setDescription("Groceries");
        t.setTransactionDate(LocalDateTime.of(2026, 10, 5, 10, 0));
        t.setCreatedAt(LocalDateTime.of(2026, 10, 5, 10, 1));
        t.setUpdatedAt(LocalDateTime.of(2026, 10, 5, 10, 1));
        t.setContentHash("internal-hash-must-not-leak");
        return t;
    }

    @Test
    void create_returns201WithLocationAndDto_userIdComesFromToken() throws Exception {
        when(transactionService.createTransaction(any())).thenReturn(transaction(5L));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category":"Food","type":"EXPENSE","amount":1200.50,
                                 "transactionDate":"2026-10-05T10:00:00","userId":999}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/transactions/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.transactionDate").value("2026-10-05T10:00:00"))
                .andExpect(jsonPath("$.contentHash").doesNotExist());

        // a client-supplied userId is overwritten with the authenticated user's id
        verify(transactionService).createTransaction(argThat(dto -> dto.getUserId() == USER_ID));
    }

    @Test
    void getById_returnsDtoWithTransactionDate_andNoEntityInternals() throws Exception {
        when(transactionService.getTransactionById(5L)).thenReturn(transaction(5L));

        mockMvc.perform(get("/api/transactions/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1200.5))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.transactionDate").value("2026-10-05T10:00:00"))
                .andExpect(jsonPath("$.contentHash").doesNotExist());
    }

    @Test
    void paginated_keepsPageShape_withDtoContent() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionService.getTransactions(any(Pageable.class), any(TransactionFilterDTO.class)))
                .thenReturn(new PageImpl<>(List.of(transaction(5L)), pageable, 1));

        mockMvc.perform(post("/api/transactions/filter/paginated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].transactionDate").value("2026-10-05T10:00:00"))
                .andExpect(jsonPath("$.content[0].contentHash").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    private Pageable pageableSentToService(String query) throws Exception {
        // Build the page from the resolved Pageable: an unpaged PageImpl cannot be serialized to JSON
        // (Unpaged.getPageNumber() throws UnsupportedOperationException)
        when(transactionService.getTransactions(any(Pageable.class), any(TransactionFilterDTO.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(0, Pageable.class), 0));

        mockMvc.perform(post("/api/transactions/filter/paginated" + query)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionService).getTransactions(captor.capture(), any(TransactionFilterDTO.class));
        return captor.getValue();
    }

    @Test
    void paginated_sortParamAsSentByAndroid_isParsedAsPropertyAndDirection() throws Exception {
        Pageable pageable = pageableSentToService("?page=1&size=5&sort=transactionDate,desc");

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    @Test
    void paginated_withoutParams_defaultsToFirstPageNewestFirst() throws Exception {
        Pageable pageable = pageableSentToService("");

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    @Test
    void categories_returnsTheUsersDistinctCategories() throws Exception {
        when(transactionService.getCategoriesForUser()).thenReturn(List.of("Food", "Rent"));

        mockMvc.perform(get("/api/transactions/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Food"))
                .andExpect(jsonPath("$[1]").value("Rent"));
    }
}

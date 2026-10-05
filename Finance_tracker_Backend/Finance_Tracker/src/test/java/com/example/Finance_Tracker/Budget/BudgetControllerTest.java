package com.example.Finance_Tracker.Budget;

import com.example.Finance_Tracker.Budget.controller.BudgetController;
import com.example.Finance_Tracker.Budget.dto.BudgetResponseDTO;
import com.example.Finance_Tracker.Budget.service.BudgetService;
import com.example.Finance_Tracker.Core.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pins the HTTP contract of POST /api/budgets. */
@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    @Mock private BudgetService budgetService;
    @InjectMocks private BudgetController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void create_returns201WithLocationAndBody() throws Exception {
        when(budgetService.createBudget(any())).thenReturn(BudgetResponseDTO.builder()
                .id(9L).name("Food Oct").amount(new BigDecimal("1000.00")).build());

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Food Oct","category":"Food","amount":1000,
                                 "startDate":"2026-10-01","endDate":"2026-10-31","frequency":"MONTHLY"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/budgets/9"))
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.name").value("Food Oct"));
    }

    @Test
    void create_withEndBeforeStart_returns400_andServiceIsNotCalled() throws Exception {
        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bad","amount":10,
                                 "startDate":"2026-10-31","endDate":"2026-10-01","frequency":"NONE"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(budgetService);
    }
}

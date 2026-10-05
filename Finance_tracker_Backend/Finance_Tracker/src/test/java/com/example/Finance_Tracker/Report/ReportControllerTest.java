package com.example.Finance_Tracker.Report;

import com.example.Finance_Tracker.Core.exception.GlobalExceptionHandler;
import com.example.Finance_Tracker.Report.controller.ReportController;
import com.example.Finance_Tracker.Report.dto.MonthlyReportDTO;
import com.example.Finance_Tracker.Report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pins the HTTP contract of the report endpoints (parameter binding and error shape). */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock private ReportService reportService;
    @InjectMocks private ReportController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void monthly_bindsMonthNameAndYear_andReturnsTheReport() throws Exception {
        when(reportService.getMonthlyReport(Month.OCTOBER, 2026)).thenReturn(new MonthlyReportDTO(
                2026, Month.OCTOBER, new BigDecimal("1000.00"), new BigDecimal("150.00"), new BigDecimal("850.00"),
                Map.of("Food", new BigDecimal("150.00"))));

        mockMvc.perform(get("/api/reports/monthly").param("month", "OCTOBER").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("OCTOBER"))
                .andExpect(jsonPath("$.netSavings").value(850.0))
                .andExpect(jsonPath("$.categoryBreakdown.Food").value(150.0));
    }

    @Test
    void monthly_withInvalidMonth_returns400_andServiceIsNotCalled() throws Exception {
        mockMvc.perform(get("/api/reports/monthly").param("month", "13").param("year", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("month")));

        verifyNoInteractions(reportService);
    }

    @Test
    void trend_withoutPeriod_usesSixMonthDefault() throws Exception {
        when(reportService.getTrendReport("6m")).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(reportService).getTrendReport("6m");
    }
}

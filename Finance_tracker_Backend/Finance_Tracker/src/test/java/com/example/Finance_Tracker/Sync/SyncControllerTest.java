package com.example.Finance_Tracker.Sync;

import com.example.Finance_Tracker.Core.exception.GlobalExceptionHandler;
import com.example.Finance_Tracker.Sync.controller.SyncController;
import com.example.Finance_Tracker.Sync.dto.SyncResponseDTO;
import com.example.Finance_Tracker.Sync.service.SyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pins the HTTP contract of GET /api/sync. */
@ExtendWith(MockitoExtension.class)
class SyncControllerTest {

    @Mock private SyncService syncService;
    @InjectMocks private SyncController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void withoutCursor_returnsTheSyncResponse() throws Exception {
        when(syncService.sync(null)).thenReturn(
                new SyncResponseDTO("2026-10-05T10:00", true, List.of(), List.of(), List.of()));

        mockMvc.perform(get("/api/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursor").value("2026-10-05T10:00"))
                .andExpect(jsonPath("$.fullSync").value(true))
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.deletedTransactionIds").isArray())
                .andExpect(jsonPath("$.budgets").isArray());
    }

    @Test
    void invalidCursor_returns400InApiErrorShape() throws Exception {
        when(syncService.sync("bad")).thenThrow(new IllegalArgumentException("Invalid sync cursor"));

        mockMvc.perform(get("/api/sync").param("cursor", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sync cursor"));
    }
}

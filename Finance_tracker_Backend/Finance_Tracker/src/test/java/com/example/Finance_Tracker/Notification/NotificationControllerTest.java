package com.example.Finance_Tracker.Notification;

import com.example.Finance_Tracker.Core.exception.GlobalExceptionHandler;
import com.example.Finance_Tracker.Notification.controller.NotificationController;
import com.example.Finance_Tracker.Notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pins the HTTP contract of the notification state changes: 204 with an empty body. */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock private NotificationService notificationService;
    @InjectMocks private NotificationController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void markAsRead_returns204() throws Exception {
        mockMvc.perform(post("/api/notifications/5/mark-as-read"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(notificationService).markAsRead(5L);
    }

    @Test
    void markAllAsRead_returns204() throws Exception {
        mockMvc.perform(post("/api/notifications/mark-all-as-read"))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllAsRead();
    }

    @Test
    void archive_returns204() throws Exception {
        mockMvc.perform(post("/api/notifications/5/archive"))
                .andExpect(status().isNoContent());

        verify(notificationService).archiveNotification(5L);
    }

    @Test
    void archiveBulk_returns204() throws Exception {
        mockMvc.perform(post("/api/notifications/archive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[5,6]"))
                .andExpect(status().isNoContent());

        verify(notificationService).archiveNotifications(List.of(5L, 6L));
    }
}

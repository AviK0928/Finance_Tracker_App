package com.example.Finance_Tracker.Settings;

import com.example.Finance_Tracker.Core.exception.GlobalExceptionHandler;
import com.example.Finance_Tracker.Security.JWTService;
import com.example.Finance_Tracker.Settings.controller.UserSettingController;
import com.example.Finance_Tracker.Settings.service.UserSettingService;
import com.example.Finance_Tracker.User.entity.BlacklistedToken;
import com.example.Finance_Tracker.User.repository.BlacklistedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pins the HTTP contract of the settings state changes: 204 with an empty body. */
@ExtendWith(MockitoExtension.class)
class UserSettingControllerTest {

    @Mock private UserSettingService settingService;
    @Mock private BlacklistedTokenRepository blacklistedTokenRepository;
    @Mock private JWTService jwtService;
    @InjectMocks private UserSettingController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateSettings_returns204() throws Exception {
        mockMvc.perform(put("/api/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{"key":"DEFAULT_CURRENCY","value":"INR"}]
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(settingService).updateSettings(anyList());
    }

    @Test
    void resetToDefaults_returns204() throws Exception {
        mockMvc.perform(post("/api/settings/reset-to-defaults"))
                .andExpect(status().isNoContent());

        verify(settingService).resetSettingsToDefault();
    }

    @Test
    void logout_blacklistsBearerToken_andReturns204() throws Exception {
        when(blacklistedTokenRepository.existsByToken("abc")).thenReturn(false);
        when(jwtService.extractExpiration("abc")).thenReturn(new Date(System.currentTimeMillis() + 60_000));

        mockMvc.perform(post("/api/settings/logout").header("Authorization", "Bearer abc"))
                .andExpect(status().isNoContent());

        ArgumentCaptor<BlacklistedToken> saved = ArgumentCaptor.forClass(BlacklistedToken.class);
        verify(blacklistedTokenRepository).save(saved.capture());
        assertEquals("abc", saved.getValue().getToken());
    }

    @Test
    void logout_withoutBearerToken_returns204_andSavesNothing() throws Exception {
        mockMvc.perform(post("/api/settings/logout"))
                .andExpect(status().isNoContent());

        verify(blacklistedTokenRepository, never()).save(any());
    }
}

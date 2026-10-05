package com.example.Finance_Tracker.User;

import com.example.Finance_Tracker.Core.exception.GlobalExceptionHandler;
import com.example.Finance_Tracker.User.controller.UserController;
import com.example.Finance_Tracker.User.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pins the HTTP contract of the password reset endpoints (JSON bodies, not plain text). */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void forgotPassword_returnsJsonMessage() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"someone@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("If the email exists, a password reset link has been sent."));

        verify(userService).initiateForgotPassword(any());
    }

    @Test
    void resetPassword_returnsJsonMessage() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"abc","newPassword":"Secret1!x"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Password has been successfully reset."));

        verify(userService).resetPassword("abc", "Secret1!x");
    }

    @Test
    void resetPassword_withInvalidToken_returns400ApiError() throws Exception {
        doThrow(new IllegalArgumentException("Invalid or expired reset token"))
                .when(userService).resetPassword("bad", "Secret1!x");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"bad","newPassword":"Secret1!x"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired reset token"));
    }
}

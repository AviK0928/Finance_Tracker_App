package com.example.Finance_Tracker.Device;

import com.example.Finance_Tracker.Core.exception.GlobalExceptionHandler;
import com.example.Finance_Tracker.Device.controller.DeviceController;
import com.example.Finance_Tracker.Device.dto.DeviceRegistrationDTO;
import com.example.Finance_Tracker.Device.entity.DevicePlatform;
import com.example.Finance_Tracker.Device.service.DeviceTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pins the device registration contract: 204 with an empty body, 400 with field errors. */
@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    @Mock private DeviceTokenService deviceTokenService;
    @InjectMocks private DeviceController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_returns204() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"abc:DEF-123_x","platform":"ANDROID"}
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deviceTokenService).register(new DeviceRegistrationDTO("abc:DEF-123_x", DevicePlatform.ANDROID));
    }

    @Test
    void register_withBlankToken_returns400_andServiceIsNotCalled() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"","platform":"ANDROID"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.token").exists());

        verifyNoInteractions(deviceTokenService);
    }

    @Test
    void unregister_returns204() throws Exception {
        mockMvc.perform(delete("/api/devices/abc:DEF-123_x"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deviceTokenService).unregister("abc:DEF-123_x");
    }
}

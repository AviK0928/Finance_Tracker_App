package com.example.Finance_Tracker.Device.dto;

import com.example.Finance_Tracker.Device.entity.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationDTO {

    @NotBlank(message = "Token is required")
    @Size(max = 4096, message = "Token is too long")
    private String token;

    @NotNull(message = "Platform is required")
    private DevicePlatform platform;
}

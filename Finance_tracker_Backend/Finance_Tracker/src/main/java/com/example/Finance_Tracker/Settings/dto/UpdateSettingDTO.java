package com.example.Finance_Tracker.Settings.dto;

import com.example.Finance_Tracker.Settings.util.SettingKey;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSettingDTO {
    @NotNull
    private SettingKey key;

    @NotNull
    private String value;
}

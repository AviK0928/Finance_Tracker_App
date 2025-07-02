package com.example.Finance_Tracker.Settings.dto;

import com.example.Finance_Tracker.Settings.util.SettingKey;
import lombok.Data;

@Data
public class UserSettingDTO {
    private SettingKey key;
    private String value;
}

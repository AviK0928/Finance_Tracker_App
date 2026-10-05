package com.example.Finance_Tracker.Settings.service;

import com.example.Finance_Tracker.Budget.entity.Budget;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.Settings.dto.ImportSummaryDTO;
import com.example.Finance_Tracker.Settings.dto.UpdateSettingDTO;
import com.example.Finance_Tracker.Settings.dto.UserSettingDTO;
import com.example.Finance_Tracker.Settings.entity.UserSetting;
import com.example.Finance_Tracker.Settings.repository.UserSettingRepository;
import com.example.Finance_Tracker.Settings.util.SettingKey;
import com.example.Finance_Tracker.Transaction.entity.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class UserSettingService {

    @Autowired
    private UserSettingRepository settingRepository;

    @Autowired
    private ExportService exportService;

    @Autowired
    private ImportService importService;


    private static final Map<SettingKey, String> DEFAULTS = Map.of(
            SettingKey.AUTO_SYNC_ENABLED, "true",
            SettingKey.SYNC_FREQUENCY_MINUTES, "60",
            SettingKey.NOTIFICATIONS_ENABLED, "true",
            SettingKey.NOTIFY_SYNC_EVENTS, "true",
            SettingKey.NOTIFY_BUDGET_EXPIRY, "true",
            SettingKey.NOTIFY_SPENDING_ALERTS, "true",
            SettingKey.DEFAULT_BUDGET_DURATION, "MONTHLY",
            SettingKey.DEFAULT_CURRENCY, "INR"
    );

    public List<UserSettingDTO> getAllSettingsForCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<UserSetting> settings = settingRepository.findByUserId(userId);

        Map<SettingKey, String> all = new EnumMap<>(DEFAULTS);
        for (UserSetting setting : settings) {
            all.put(setting.getKey(), setting.getValue());
        }

        return all.entrySet().stream().map(entry -> {
            UserSettingDTO dto = new UserSettingDTO();
            dto.setKey(entry.getKey());
            dto.setValue(entry.getValue());
            return dto;
        }).toList();
    }

    @Transactional
    public void updateSettings(List<UpdateSettingDTO> dtos) {
        Long userId = SecurityUtils.getCurrentUserId();

        for (UpdateSettingDTO dto : dtos) {
            settingRepository.findByUserIdAndKey(userId, dto.getKey())
                    .ifPresentOrElse(
                            setting -> setting.setValue(dto.getValue()),
                            () -> {
                                UserSetting newSetting = UserSetting.builder()
                                        .userId(userId)
                                        .key(dto.getKey())
                                        .value(dto.getValue())
                                        .build();
                                settingRepository.save(newSetting);
                            });
        }
    }

    public boolean getBoolean(SettingKey key) {
        String value = getRawValue(key);
        return Boolean.parseBoolean(value);
    }

    public int getInt(SettingKey key) {
        return Integer.parseInt(getRawValue(key));
    }

    public String getRawValue(SettingKey key) {
        return getRawValueForUser(SecurityUtils.getCurrentUserId(), key);
    }

    /** For callers without a logged-in user (scheduler threads, notifications for a given owner). */
    public boolean getBooleanForUser(Long userId, SettingKey key) {
        return Boolean.parseBoolean(getRawValueForUser(userId, key));
    }

    private String getRawValueForUser(Long userId, SettingKey key) {
        return settingRepository.findByUserIdAndKey(userId, key)
                .map(UserSetting::getValue)
                .orElse(DEFAULTS.get(key));
    }
    public void resetSettingsToDefault() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<UserSetting> existing = settingRepository.findByUserId(userId);
        settingRepository.deleteAll(existing);
    }

    public byte[] exportDataForUser(Long userId) {
        return exportService.exportUserData(userId);
    }

    public String getExportFilename() {
        return exportService.generateExportFilename();
    }

    public ImportSummaryDTO importDataForUser(MultipartFile file, Long userId) {
        return importService.importUserData(file, userId);
    }

    public List<UserSettingDTO> getAllSettingsForUser(Long userId) {
        List<UserSetting> settings = settingRepository.findByUserId(userId);

        Map<SettingKey, String> all = new EnumMap<>(DEFAULTS);
        for (UserSetting setting : settings) {
            all.put(setting.getKey(), setting.getValue());
        }

        return all.entrySet().stream().map(entry -> {
            UserSettingDTO dto = new UserSettingDTO();
            dto.setKey(entry.getKey());
            dto.setValue(entry.getValue());
            return dto;
        }).toList();
    }
}

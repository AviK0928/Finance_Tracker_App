package com.example.Finance_Tracker.Settings.controller;


import com.example.Finance_Tracker.Security.JWTService;
import com.example.Finance_Tracker.Settings.dto.ImportSummaryDTO;
import com.example.Finance_Tracker.Settings.dto.UpdateSettingDTO;
import com.example.Finance_Tracker.Settings.dto.UserSettingDTO;
import com.example.Finance_Tracker.Settings.service.ImportService;
import com.example.Finance_Tracker.Settings.service.UserSettingService;
import com.example.Finance_Tracker.Security.SecurityUtils;
import com.example.Finance_Tracker.User.entity.BlacklistedToken;
import com.example.Finance_Tracker.User.repository.BlacklistedTokenRepository;
import com.example.Finance_Tracker.User.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/settings")
public class UserSettingController {

    @Autowired
    private UserSettingService settingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ImportService importService;

    @GetMapping
    public ResponseEntity<List<UserSettingDTO>> getSettings() {
        return ResponseEntity.ok(settingService.getAllSettingsForCurrentUser());
    }

    @PutMapping
    public ResponseEntity<Void> updateSettings(@RequestBody List<@Valid UpdateSettingDTO> settings) {
        settingService.updateSettings(settings);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-to-defaults")
    public ResponseEntity<Void> resetToDefaults() {
        settingService.resetSettingsToDefault();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (!blacklistedTokenRepository.existsByToken(token)) {

                LocalDateTime expiry = jwtService.extractExpiration(token)
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                BlacklistedToken blacklisted = BlacklistedToken.builder()
                        .token(token)
                        .blacklistedAt(LocalDateTime.now())
                        .expiry(expiry)
                        .build();

                blacklistedTokenRepository.save(blacklisted);
            }
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount() {
        Long userId = SecurityUtils.getCurrentUserId();
        userRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export-data")
    public ResponseEntity<byte[]> exportData() {
        Long userId = SecurityUtils.getCurrentUserId();
        byte[] zipData = settingService.exportDataForUser(userId);
        String filename = settingService.getExportFilename();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipData.length))
                .body(zipData);
    }

    @PostMapping("/import-data")
    public ResponseEntity<ImportSummaryDTO> importData(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(settingService.importDataForUser(file, userId));
    }
}
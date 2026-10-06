package com.example.Finance_Tracker.Device.service;

import com.example.Finance_Tracker.Device.dto.DeviceRegistrationDTO;
import com.example.Finance_Tracker.Device.entity.DeviceToken;
import com.example.Finance_Tracker.Device.repository.DeviceTokenRepository;
import com.example.Finance_Tracker.Security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    /**
     * Most devices one account keeps. Reinstalls and cleared app data each create a new token; FCM only
     * reports an old one as gone when it is pushed to, so without a cap the rows would only ever grow.
     */
    public static final int MAX_DEVICES_PER_USER = 10;

    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Registers or refreshes the current user's device. A token already stored for another user is moved
     * to the current user (a different account signed in on that phone), so pushes follow the account.
     * Beyond {@link #MAX_DEVICES_PER_USER}, the least recently registered devices are removed.
     */
    @Transactional
    public void register(DeviceRegistrationDTO dto) {
        Long userId = currentUserId();
        LocalDateTime now = LocalDateTime.now();
        deviceTokenRepository.findByToken(dto.getToken()).ifPresentOrElse(
                existing -> existing.assignTo(userId, dto.getPlatform(), now),
                () -> deviceTokenRepository.save(new DeviceToken(userId, dto.getToken(), dto.getPlatform(), now)));

        // The query flushes the change above first, so the device just registered counts and comes first
        List<DeviceToken> devices = deviceTokenRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        if (devices.size() > MAX_DEVICES_PER_USER) {
            deviceTokenRepository.deleteAll(devices.subList(MAX_DEVICES_PER_USER, devices.size()));
        }
    }

    /**
     * Removes the token if it belongs to the current user (called on logout). Unknown tokens and tokens of
     * other users are ignored, so logout never fails on this call and nobody can unregister another device.
     */
    public void unregister(String token) {
        deviceTokenRepository.deleteByUserIdAndToken(currentUserId(), token);
    }

    private static Long currentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return userId;
    }
}

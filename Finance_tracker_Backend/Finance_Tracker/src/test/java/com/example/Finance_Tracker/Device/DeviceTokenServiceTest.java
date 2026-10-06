package com.example.Finance_Tracker.Device;

import com.example.Finance_Tracker.Device.dto.DeviceRegistrationDTO;
import com.example.Finance_Tracker.Device.entity.DevicePlatform;
import com.example.Finance_Tracker.Device.entity.DeviceToken;
import com.example.Finance_Tracker.Device.repository.DeviceTokenRepository;
import com.example.Finance_Tracker.Device.service.DeviceTokenService;
import com.example.Finance_Tracker.User.mapper.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    private static final long CURRENT_USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;

    @Mock private DeviceTokenRepository deviceTokenRepository;
    @InjectMocks private DeviceTokenService deviceTokenService;

    @BeforeEach
    void loginAsCurrentUser() {
        CustomUserDetails principal = new CustomUserDetails(CURRENT_USER_ID, "me@example.com", "hash", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_newToken_isSavedForTheCurrentUser() {
        when(deviceTokenRepository.findByToken("token-1")).thenReturn(Optional.empty());

        deviceTokenService.register(new DeviceRegistrationDTO("token-1", DevicePlatform.ANDROID));

        ArgumentCaptor<DeviceToken> saved = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(CURRENT_USER_ID);
        assertThat(saved.getValue().getToken()).isEqualTo("token-1");
        assertThat(saved.getValue().getPlatform()).isEqualTo(DevicePlatform.ANDROID);
    }

    @Test
    void register_tokenOfAnotherAccount_movesToTheCurrentUser() {
        LocalDateTime registered = LocalDateTime.of(2026, 1, 1, 0, 0);
        DeviceToken existing = new DeviceToken(OTHER_USER_ID, "token-1", DevicePlatform.ANDROID, registered);
        when(deviceTokenRepository.findByToken("token-1")).thenReturn(Optional.of(existing));

        deviceTokenService.register(new DeviceRegistrationDTO("token-1", DevicePlatform.ANDROID));

        assertThat(existing.getUserId()).isEqualTo(CURRENT_USER_ID);
        assertThat(existing.getUpdatedAt()).isAfter(registered);
        verify(deviceTokenRepository, never()).save(any());
    }

    @Test
    void register_beyondTheCap_removesTheLeastRecentlyRegisteredDevices() {
        when(deviceTokenRepository.findByToken("token-new")).thenReturn(Optional.empty());
        LocalDateTime now = LocalDateTime.now();
        List<DeviceToken> newestFirst = new ArrayList<>();
        for (int i = 0; i <= DeviceTokenService.MAX_DEVICES_PER_USER; i++) {
            newestFirst.add(new DeviceToken(CURRENT_USER_ID, "token-" + i, DevicePlatform.ANDROID, now.minusDays(i)));
        }
        when(deviceTokenRepository.findByUserIdOrderByUpdatedAtDesc(CURRENT_USER_ID)).thenReturn(newestFirst);

        deviceTokenService.register(new DeviceRegistrationDTO("token-new", DevicePlatform.ANDROID));

        verify(deviceTokenRepository).deleteAll(List.of(newestFirst.get(DeviceTokenService.MAX_DEVICES_PER_USER)));
    }

    @Test
    void register_withinTheCap_removesNothing() {
        when(deviceTokenRepository.findByToken("token-new")).thenReturn(Optional.empty());

        deviceTokenService.register(new DeviceRegistrationDTO("token-new", DevicePlatform.ANDROID));

        verify(deviceTokenRepository, never()).deleteAll(any());
    }

    @Test
    void unregister_deletesOnlyTheCurrentUsersToken() {
        deviceTokenService.unregister("token-1");

        verify(deviceTokenRepository).deleteByUserIdAndToken(CURRENT_USER_ID, "token-1");
    }
}

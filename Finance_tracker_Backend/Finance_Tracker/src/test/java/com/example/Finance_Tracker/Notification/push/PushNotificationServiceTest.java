package com.example.Finance_Tracker.Notification.push;

import com.example.Finance_Tracker.Device.entity.DevicePlatform;
import com.example.Finance_Tracker.Device.entity.DeviceToken;
import com.example.Finance_Tracker.Device.repository.DeviceTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    private static final long USER_ID = 1L;

    @Mock private DeviceTokenRepository deviceTokenRepository;
    @Mock private PushSender pushSender;
    @InjectMocks private PushNotificationService pushNotificationService;

    @Test
    void pushesEveryDeviceOfTheUser_andDropsTokensFirebaseReportsGone() {
        DeviceToken phone = device(10L, "phone-token");
        DeviceToken uninstalled = device(11L, "uninstalled-token");
        when(deviceTokenRepository.findByUserId(USER_ID)).thenReturn(List.of(phone, uninstalled));
        when(pushSender.send("phone-token")).thenReturn(PushResult.SENT);
        when(pushSender.send("uninstalled-token")).thenReturn(PushResult.TOKEN_GONE);

        pushNotificationService.onNotificationCreated(new NotificationCreatedEvent(USER_ID));

        verify(deviceTokenRepository).deleteById(11L);
        verify(deviceTokenRepository, never()).deleteById(10L);
    }

    @Test
    void failedPush_keepsTheToken() {
        when(deviceTokenRepository.findByUserId(USER_ID)).thenReturn(List.of(device(10L, "phone-token")));
        when(pushSender.send("phone-token")).thenReturn(PushResult.FAILED);

        pushNotificationService.onNotificationCreated(new NotificationCreatedEvent(USER_ID));

        verify(deviceTokenRepository, never()).deleteById(any());
    }

    private static DeviceToken device(long id, String token) {
        DeviceToken device = new DeviceToken(USER_ID, token, DevicePlatform.ANDROID, LocalDateTime.now());
        ReflectionTestUtils.setField(device, "id", id);
        return device;
    }
}

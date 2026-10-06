package com.example.Finance_Tracker.Notification.push;

import com.example.Finance_Tracker.Device.entity.DeviceToken;
import com.example.Finance_Tracker.Device.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Pushes to every device of the user once the notification is committed (a rolled-back alert is not pushed,
 * and the app's sync after the push can already see the data). Runs on a background thread so a slow or failing
 * Firebase call never delays or breaks the request that created the notification.
 */
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushSender pushSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        for (DeviceToken device : deviceTokenRepository.findByUserId(event.userId())) {
            if (pushSender.send(device.getToken()) == PushResult.TOKEN_GONE) {
                deviceTokenRepository.deleteById(device.getId());
            }
        }
    }
}

package com.example.Finance_Tracker.Notification.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Push through Firebase Cloud Messaging. The visible text is generic on purpose: it shows on the lock screen,
 * so it never carries amounts, categories or titles. The data part only tells the app to sync and open its inbox.
 */
@Slf4j
@RequiredArgsConstructor
public class FirebasePushSender implements PushSender {

    static final String TITLE = "Quantro";
    static final String BODY = "You have a new notification";

    private final FirebaseMessaging messaging;

    @Override
    public PushResult send(String deviceToken) {
        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(Notification.builder().setTitle(TITLE).setBody(BODY).build())
                .putData("action", "sync")
                .build();
        try {
            messaging.send(message);
            return PushResult.SENT;
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.SENDER_ID_MISMATCH) {
                return PushResult.TOKEN_GONE;
            }
            // Only the error code: the token identifies a device and is not logged
            log.warn("Push failed: {}", code != null ? code : e.getErrorCode());
            return PushResult.FAILED;
        }
    }
}

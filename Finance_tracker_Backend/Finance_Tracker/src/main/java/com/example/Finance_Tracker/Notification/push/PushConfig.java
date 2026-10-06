package com.example.Finance_Tracker.Notification.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Firebase push when FIREBASE_SERVICE_ACCOUNT_JSON holds a service account key, otherwise no push
 * (local runs, tests, CI). The key comes from the environment only; it is never written to disk or logged.
 */
@Slf4j
@Configuration
public class PushConfig {

    private static final String APP_NAME = "finance-tracker-push";

    @Bean
    public PushSender pushSender(@Value("${firebase.service-account-json:}") String serviceAccountJson) throws IOException {
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            log.info("Push notifications disabled: FIREBASE_SERVICE_ACCOUNT_JSON is not set");
            return deviceToken -> PushResult.SKIPPED;
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)));
        // A named app that is reused if it already exists (devtools restarts and test contexts create the bean again)
        FirebaseApp app = FirebaseApp.getApps().stream()
                .filter(existing -> existing.getName().equals(APP_NAME))
                .findFirst()
                .orElseGet(() -> FirebaseApp.initializeApp(
                        FirebaseOptions.builder().setCredentials(credentials).build(), APP_NAME));
        log.info("Push notifications enabled");
        return new FirebasePushSender(FirebaseMessaging.getInstance(app));
    }
}

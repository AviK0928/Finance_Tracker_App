package com.example.Finance_Tracker.Notification.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** How Firebase answers map to keeping or dropping a token. No network: FirebaseMessaging is mocked. */
@ExtendWith(MockitoExtension.class)
class FirebasePushSenderTest {

    @Mock private FirebaseMessaging messaging;
    @InjectMocks private FirebasePushSender sender;

    @Test
    void accepted_isSent() throws Exception {
        when(messaging.send(any(Message.class))).thenReturn("projects/p/messages/1");

        assertThat(sender.send("token")).isEqualTo(PushResult.SENT);
    }

    @Test
    void unregisteredToken_isGone() throws Exception {
        // Built first: stubbing the exception inside another when(...) would be an unfinished stubbing
        FirebaseMessagingException error = firebaseError(MessagingErrorCode.UNREGISTERED);
        when(messaging.send(any(Message.class))).thenThrow(error);

        assertThat(sender.send("token")).isEqualTo(PushResult.TOKEN_GONE);
    }

    @Test
    void otherErrors_failWithoutDroppingTheToken() throws Exception {
        // Built first: stubbing the exception inside another when(...) would be an unfinished stubbing
        FirebaseMessagingException error = firebaseError(MessagingErrorCode.INTERNAL);
        when(messaging.send(any(Message.class))).thenThrow(error);

        assertThat(sender.send("token")).isEqualTo(PushResult.FAILED);
    }

    private static FirebaseMessagingException firebaseError(MessagingErrorCode code) {
        FirebaseMessagingException error = mock(FirebaseMessagingException.class);
        when(error.getMessagingErrorCode()).thenReturn(code);
        return error;
    }
}

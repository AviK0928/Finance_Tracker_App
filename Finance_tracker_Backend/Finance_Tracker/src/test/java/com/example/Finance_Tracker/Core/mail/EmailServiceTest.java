package com.example.Finance_Tracker.Core.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private MailProperties mailProperties;
    @InjectMocks private EmailService emailService;

    @Test
    void resetEmail_containsTheCodeAndNoPlaceholderLink() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(mailProperties.getFromName()).thenReturn("Quantro");
        when(mailProperties.getFromAddress()).thenReturn("no-reply@quantro.com");

        emailService.sendResetPasswordEmail("user@example.com", "user", "3f2b9c1e-0d4a-4f7e-9b8a-1c2d3e4f5a6b");

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(sent.capture());
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        sent.getValue().writeTo(raw);
        assertThat(raw.toString())
                .contains("3f2b9c1e-0d4a-4f7e-9b8a-1c2d3e4f5a6b")
                .doesNotContain("href");
    }
}

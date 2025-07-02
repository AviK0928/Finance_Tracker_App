package com.example.Finance_Tracker.Core.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public void sendResetPasswordEmail(String to, String username, String resetLink) {
        String subject = "Reset your Quantro password";
        String html = """
                <p>Hello %s,</p>
                <p>You requested a password reset. Click below:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>This link is valid for 15 minutes.</p>
                <p>If you didn’t request this, you can ignore it.</p>
                <p>— %s Team</p>
                """.formatted(username, resetLink, mailProperties.getFromName());

        sendEmail(to, subject, html);
    }

    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Quantro 🎉";
        String html = """
                <p>Hi %s,</p>
                <p>Welcome to <strong>%s</strong> — your personal finance tracker!</p>
                <p>We’re excited to help you manage your money better.</p>
                <p>— %s Team</p>
                """.formatted(username, mailProperties.getFromName(), mailProperties.getFromName());

        sendEmail(to, subject, html);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(mailProperties.getFromAddress(), mailProperties.getFromName());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Email sending failed", e);
        }
    }
}

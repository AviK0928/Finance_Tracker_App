package com.example.Finance_Tracker.User.service;

import com.example.Finance_Tracker.User.entity.PasswordResetToken;
import com.example.Finance_Tracker.User.entity.User;
import com.example.Finance_Tracker.User.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;

    public String generateResetToken(User user) {
        tokenRepo.deleteByUser(user); // clean old tokens

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiry(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepo.save(resetToken);
        return token;
    }

    public PasswordResetToken validateToken(String token) {
        return tokenRepo.findByToken(token)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiry().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    public void markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepo.save(token);
    }
}

package com.example.Finance_Tracker.User.repository;

import com.example.Finance_Tracker.User.entity.PasswordResetToken;
import com.example.Finance_Tracker.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
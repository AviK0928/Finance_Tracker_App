package com.example.Finance_Tracker.User.repository;

import com.example.Finance_Tracker.User.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    boolean existsByToken(String token);
    long count();  // from JpaRepository
    void deleteByExpiryDateBefore(LocalDateTime now);
}

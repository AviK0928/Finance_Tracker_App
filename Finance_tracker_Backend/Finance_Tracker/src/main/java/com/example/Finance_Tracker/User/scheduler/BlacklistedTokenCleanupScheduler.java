package com.example.Finance_Tracker.User.scheduler;

import com.example.Finance_Tracker.User.repository.BlacklistedTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class BlacklistedTokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistedTokenCleanupScheduler.class);

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    /**
     * Runs weekly at 2:00 AM on Mondays to delete expired blacklisted tokens.
     */
    @Scheduled(cron = "0 0 2 * * MON")  // Every Monday at 2:00 AM
    @Transactional
    public void cleanUpExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("[Token Cleanup] Starting cleanup at {} for tokens expired before now.", now);

        long countBefore = blacklistedTokenRepository.count();
        blacklistedTokenRepository.deleteByExpiryBefore(now);
        long countAfter = blacklistedTokenRepository.count();

        long deleted = countBefore - countAfter;
        logger.info("[Token Cleanup] Deleted {} expired blacklisted tokens. Remaining tokens: {}", deleted, countAfter);
    }
}
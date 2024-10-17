package com.multicloud.auth.service;

import com.multicloud.auth.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Logger logger = LoggerFactory.getLogger(TokenCleanupService.class);
    public TokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Schedule this method to run once a day (e.g., every 24 hours)
    @Scheduled(cron = "0 0 0 * * ?")  // This cron runs every day at midnight
    @Transactional
    public void cleanOldRefreshTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);  // Tokens older than 30 days
        int deletedCount = refreshTokenRepository.deleteByCreatedAtBefore(cutoffDate);
        logger.info("Deleted {} old refresh tokens", deletedCount);
    }
}
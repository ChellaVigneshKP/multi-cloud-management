package com.multicloud.auth.service;

import com.multicloud.auth.model.RefreshToken;
import com.multicloud.auth.model.User;
import com.multicloud.auth.repository.RefreshTokenRepository;
import com.multicloud.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TokenCleanupServiceIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenCleanupService tokenCleanupService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create a user instance with required fields set
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser@example.com"); // Set required non-nullable email
        user.setPassword("testPassword"); // Set required non-nullable password
        userRepository.save(user);

        // Create expired tokens (older than 30 days)
        refreshTokenRepository.save(new RefreshToken(user, "expired-token-1", LocalDateTime.now().minusDays(31), null, null, null));
        refreshTokenRepository.save(new RefreshToken(user, "expired-token-2", LocalDateTime.now().minusDays(40), null, null, null));

        // Create a valid token (less than 30 days expired)
        refreshTokenRepository.save(new RefreshToken(user, "valid-token", LocalDateTime.now().minusDays(29), null, null, null));
    }

    @Test
    void cleanOldRefreshTokens_ShouldDeleteTokensExpiredMoreThan30Days() {
        // Act
        tokenCleanupService.cleanOldRefreshTokens();

        // Assert
        List<RefreshToken> remainingTokens = refreshTokenRepository.findAll();
        assertThat(remainingTokens).hasSize(1);
        assertThat(remainingTokens.get(0).getToken()).isEqualTo("valid-token");
    }
}
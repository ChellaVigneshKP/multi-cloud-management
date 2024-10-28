package com.multicloud.auth.service;

import com.multicloud.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class TokenCleanupServiceTest {
    private AutoCloseable closeable;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenCleanupService tokenCleanupService;

    @BeforeEach
    void setUp() {
        // Use AutoCloseable with try-with-resources for proper resource management
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void cleanOldRefreshTokens_ShouldDeleteExpiredTokens() {
        // Arrange
        int expectedDeletedCount = 5;

        // Mock the repository call with any LocalDateTime argument
        when(refreshTokenRepository.deleteByExpiryDateBefore(any(LocalDateTime.class))).thenReturn(expectedDeletedCount);

        // Act
        tokenCleanupService.cleanOldRefreshTokens();

        // Assert
        verify(refreshTokenRepository, times(1)).deleteByExpiryDateBefore(any(LocalDateTime.class));
    }
}

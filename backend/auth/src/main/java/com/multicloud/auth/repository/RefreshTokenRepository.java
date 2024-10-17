package com.multicloud.auth.repository;

import com.multicloud.auth.model.RefreshToken;
import com.multicloud.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserAndIpAddress(User user, String ipAddress);  // Query for existing token by user and IP
    void deleteByToken(String token);
    int deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
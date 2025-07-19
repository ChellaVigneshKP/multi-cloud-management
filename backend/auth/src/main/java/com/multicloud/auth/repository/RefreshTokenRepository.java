package com.multicloud.auth.repository;

import com.multicloud.auth.entity.RefreshToken;
import com.multicloud.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserAndVisitorId(User user, String visitorId);
    int deleteByCreatedAtBefore(LocalDateTime cutoffDate);
    boolean existsByToken(String token);
    int deleteByExpiryDateBefore(LocalDateTime cutoffDate);
}
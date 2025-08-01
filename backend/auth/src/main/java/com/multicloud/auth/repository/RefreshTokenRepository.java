package com.multicloud.auth.repository;

import com.multicloud.auth.entity.RefreshToken;
import com.multicloud.auth.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserAndVisitorId(User user, String visitorId);

    boolean existsByUserAndVisitorId(User user, String visitorId);
    boolean existsByUserAndIpAddress(User user, String ipAddress);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < ?1")
    int deleteExpiredTokens(LocalDateTime cutoffDate);

    boolean existsByToken(String token);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt, rt.revokedByIp = :revokedByIp " +
            "WHERE rt.user = :user")
    int revokeAllForUser(@Param("user") User user,
                         @Param("revokedAt") LocalDateTime revokedAt,
                         @Param("revokedByIp") String revokedByIp);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now")
    long countActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt, rt.revokedByIp = :revokedByIp " +
            "WHERE rt.id = :tokenId AND rt.user = :user")
    int revokeTokenForUser(@Param("tokenId") Long tokenId,
                           @Param("user") User user,
                           @Param("revokedAt") LocalDateTime revokedAt,
                           @Param("revokedByIp") String revokedByIp);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt, rt.revokedByIp = :revokedByIp " +
            "WHERE rt.user = :user AND rt.id != :exceptTokenId")
    int revokeAllForUserExcept(@Param("user") User user,
                               @Param("exceptTokenId") Long exceptTokenId,
                               @Param("revokedAt") LocalDateTime revokedAt,
                               @Param("revokedByIp") String revokedByIp);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.visitorId = :visitorId")
    Optional<RefreshToken> lockByUserAndVisitorId(@Param("user") User user, @Param("visitorId") String visitorId);


    // Add these new methods
    Optional<RefreshToken> findByTokenAndUser(String token, User user);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.visitorId = :visitorId AND rt.revoked = false AND rt.expiryDate > :now")
    long countActiveTokensByUserAndVisitorId(@Param("user") User user, @Param("visitorId") String visitorId, @Param("now") LocalDateTime now);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now ORDER BY rt.expiryDate DESC")
    List<RefreshToken> findActiveTokensByUserOrdered(@Param("user") User user, @Param("now") LocalDateTime now);

}
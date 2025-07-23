package com.multicloud.auth.repository;

import com.multicloud.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<User> findByVerificationCode(String verificationCode);
    Optional<User> findByPasswordResetToken(String token);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true")
    Optional<User> findEnabledByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
    Optional<User> findEnabledByUsername(@Param("username") String username);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = :attempts WHERE u.id = :userId")
    void updateFailedAttempts(@Param("userId") Long userId, @Param("attempts") int attempts);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.id = :userId")
    void incrementFailedAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.locked = :locked, u.lockoutEnd = :lockoutEnd WHERE u.id = :userId")
    void updateLockStatus(@Param("userId") Long userId,
                          @Param("locked") boolean locked,
                          @Param("lockoutEnd") LocalDateTime lockoutEnd);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin, u.lastLoginIp = :ip WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId,
                         @Param("lastLogin") LocalDateTime lastLogin,
                         @Param("ip") String ip);


    // Add this new method
    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1, " +
            "u.locked = CASE WHEN (u.failedAttempts + 1) >= :maxAttempts THEN true ELSE u.locked END, " +
            "u.lockoutEnd = CASE WHEN (u.failedAttempts + 1) >= :maxAttempts THEN :lockoutEnd ELSE u.lockoutEnd END " +
            "WHERE u.id = :userId")
    void incrementFailedAttemptsAndLockIfNeeded(
            @Param("userId") Long userId,
            @Param("maxAttempts") int maxAttempts,
            @Param("lockoutEnd") LocalDateTime lockoutEnd);

    @Modifying
    @Query("UPDATE User u SET " +
            "u.failedAttempts = :newAttempts, " +
            "u.locked = :locked, " +
            "u.lockoutEnd = :lockoutEnd " +
            "WHERE u.id = :userId")
    void updateFailedAttemptsAndLockStatus(
            @Param("userId") Long userId,
            @Param("newAttempts") int newAttempts,
            @Param("locked") boolean locked,
            @Param("lockoutEnd") LocalDateTime lockoutEnd);
}
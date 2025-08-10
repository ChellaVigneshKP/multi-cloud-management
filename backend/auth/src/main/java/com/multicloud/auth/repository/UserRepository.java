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

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin, u.lastLoginIp = :ip WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId,
                         @Param("lastLogin") LocalDateTime lastLogin,
                         @Param("ip") String ip);


    @Modifying
    @Query("UPDATE User u SET " +
            "u.failedAttempts = :newAttempts, " +
            "u.temporarilyLocked = :locked, " +
            "u.lockoutEnd = :lockoutEnd " +
            "WHERE u.id = :userId")
    void updateFailedAttemptsAndLockStatus(
            @Param("userId") Long userId,
            @Param("newAttempts") int newAttempts,
            @Param("locked") boolean locked,
            @Param("lockoutEnd") LocalDateTime lockoutEnd);

    @Query("""
    SELECT DISTINCT u
    FROM User u
    LEFT JOIN FETCH u.roles r
    LEFT JOIN FETCH r.permissions
    WHERE u.email = :username OR u.username = :username
""")
    Optional<User> findByEmailOrUsernameWithRoles(@Param("username") String username);
}
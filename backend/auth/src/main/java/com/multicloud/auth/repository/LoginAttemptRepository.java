package com.multicloud.auth.repository;

import com.multicloud.auth.entity.LoginAttempt;
import com.multicloud.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByUserOrderByAttemptTimeDesc(User user);

    @Query("SELECT la FROM LoginAttempt la WHERE la.user = :user AND la.attemptTime > :since ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findRecentByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ip AND la.attemptTime > :since AND la.successful = false")
    long countFailedAttemptsByIpSince(@Param("ip") String ip, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.user = :user AND la.attemptTime > :since AND la.successful = false")
    long countFailedAttemptsByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
}
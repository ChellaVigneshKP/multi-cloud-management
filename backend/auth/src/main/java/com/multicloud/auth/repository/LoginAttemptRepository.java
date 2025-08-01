package com.multicloud.auth.repository;

import com.multicloud.auth.entity.LoginAttempt;
import com.multicloud.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByUserOrderByAttemptTimeDesc(User user);

    @Query("SELECT COUNT(DISTINCT la.visitorId) FROM LoginAttempt la WHERE la.email = :email AND la.successful = false AND la.attemptTime > :since")
    long countDistinctFailedVisitorIds(@Param("email") String email, @Param("since") LocalDateTime since);


    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.visitorId = :visitorId AND la.attemptTime > :since AND la.successful = false")
    long countFailedAttemptsByVisitorId(@Param("email") String email, @Param("visitorId") String visitorId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.ipAddress != :ip AND la.visitorId != :visitorId AND la.attemptTime > :since AND la.successful = false")
    long countFallbackAttemptsFromOtherDevices(@Param("email") String email, @Param("ip") String ip, @Param("visitorId") String visitorId, @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE la.email = :email AND la.attemptTime > :since AND la.successful = false")
    void deleteFailedAttemptsByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);

    @Query("""
                SELECT la FROM LoginAttempt la
                WHERE la.email = :email
                  AND la.successful = false
                  AND la.visitorId != :visitorId
                  AND la.attemptTime > :since
                ORDER BY la.attemptTime DESC
            """)
    List<LoginAttempt> findRecentFailedAttemptsByEmailExcludingIpAndVisitorId(
            @Param("email") String email,
            @Param("visitorId") String visitorId,
            @Param("since") LocalDateTime since);
}
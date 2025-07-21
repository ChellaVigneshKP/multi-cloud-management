package com.multicloud.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime attemptTime;

    @Column(nullable = false)
    private boolean successful;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    @Column(length = 255)
    private String failureReason;

    @Version
    private Long version;

    // Constructors
    public LoginAttempt() {
        this.attemptTime = LocalDateTime.now();
    }

    public LoginAttempt(User user, String email, boolean successful, String ipAddress, String userAgent, String failureReason) {
        this();
        this.user = user;
        this.email = email;
        this.successful = successful;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.failureReason = failureReason;
    }
}
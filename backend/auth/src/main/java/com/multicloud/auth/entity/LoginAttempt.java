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

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(name = "login_method", length = 20) // EMAIL, MOBILE, USERNAME
    private String loginMethod;

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

    @Column(name = "visitor_id", length = 64)
    private String visitorId;

    @Column(name = "two_factor_method", length = 20) // SMS, EMAIL, AUTHENTICATOR
    private String twoFactorMethod;

    @Column(name = "two_factor_success")
    private Boolean twoFactorSuccess;

    @Version
    private Long version;

    public LoginAttempt() {
        this.attemptTime = LocalDateTime.now();
    }

    public LoginAttempt(User user, String email, String mobileNumber, String loginMethod,
                        boolean successful, String ipAddress, String userAgent,
                        String failureReason, String visitorId, String twoFactorMethod,
                        Boolean twoFactorSuccess) {
        this();
        this.user = user;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.loginMethod = loginMethod;
        this.successful = successful;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.failureReason = failureReason;
        this.visitorId = visitorId;
        this.twoFactorMethod = twoFactorMethod;
        this.twoFactorSuccess = twoFactorSuccess;
    }

    public LoginAttempt(User user, String email, boolean successful, String ip, String userAgent, String failureReason, String visitorId) {
        this();
        this.user = user;
        this.email = email;
        this.successful = successful;
        this.ipAddress = ip;
        this.userAgent = userAgent;
        this.failureReason = failureReason;
        this.visitorId = visitorId;
    }
}
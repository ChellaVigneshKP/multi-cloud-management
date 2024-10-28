package com.multicloud.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "device_info", nullable = true)
    private String deviceInfo;

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;

    @Column(name = "visitor_id", nullable = true)
    private String visitorId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RefreshToken() {
        // No-argument constructor is required by JPA
    }

    public RefreshToken(User user, String token, LocalDateTime expiryDate, String deviceInfo, String ipAddress, String visitorId) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.visitorId = visitorId;
        this.createdAt = LocalDateTime.now();  // Set createdAt during explicit creation
    }

    @PrePersist
    public void prePersist() {
        // Set createdAt if not already set
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public void updateToken(String newToken, LocalDateTime newExpiryDate) {
        this.token = newToken;
        this.expiryDate = newExpiryDate;
    }
}

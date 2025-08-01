package com.multicloud.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_token", columnList = "token"),
                @Index(name = "idx_user_visitor", columnList = "user_id,visitor_id"),
                @Index(name = "idx_revoked_expiry", columnList = "revoked,expiry_date"),
                @Index(name = "idx_token_user", columnList = "token,user_id")
        })
@Getter
@Setter
@ToString(exclude = "user")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    @NotNull
    @Size(min = 36, max = 36)
    private String token;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime expiryDate;

    @Column(name = "device_info", length = 512)
    @Size(max = 512)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    @NotNull
    @Size(max = 45)
    private String ipAddress;

    @Column(name = "visitor_id", length = 64)
    @Size(max = 64)
    private String visitorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by_ip", length = 45)
    @Size(max = 45)
    private String revokedByIp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Version
    private Long version;

    public RefreshToken() {}

    public RefreshToken(User user, String token, LocalDateTime expiryDate,
                        String deviceInfo, String ipAddress, String visitorId) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.visitorId = visitorId;
    }

    public boolean isExpired() {
        return expiryDate == null || LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    public void updateToken(String newToken, LocalDateTime newExpiryDate, String newIpAddress) {
        this.token = newToken;
        this.expiryDate = newExpiryDate;
        this.ipAddress = newIpAddress;
    }

    public void revoke(String revokedByIp) {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revokedByIp = revokedByIp;
    }

    public boolean isRecentlyRevoked() {
        return revoked && revokedAt != null && revokedAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    public boolean isAboutToExpire() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now().plusDays(1));
    }
}
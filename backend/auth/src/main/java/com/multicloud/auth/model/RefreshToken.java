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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // Track the first login time for each token

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RefreshToken() {}

    public RefreshToken(User user, String token, LocalDateTime expiryDate, String deviceInfo, String ipAddress) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now();  // Set the creation time when the token is generated
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}

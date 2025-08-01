package com.multicloud.auth.entity;

import com.multicloud.commonlib.annotations.masksensitive.MaskSensitive;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_lock_status", columnList = "locked,lockoutEnd")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @NotNull
    @Size(min = 3, max = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    @NotNull
    @Email
    @Size(max = 100)
    @MaskSensitive(partial = true)
    private String email;

    @Column(nullable = false, length = 255)
    @NotNull
    @Size(min = 8, max = 255)
    private String password;

    @Column(name = "first_name", length = 50)
    @Size(max = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    @Size(max = 50)
    private String lastName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "verification_code", length = 64)
    @Size(max = 64)
    private String verificationCode;

    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(name = "password_reset_token", length = 64)
    @Size(max = 64)
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_login_ip", length = 45)
    @Size(max = 45)
    private String lastLoginIp;

    @Column(name = "failed_attempts")
    private int failedAttempts = 0;

    @Column(name = "locked")
    private boolean locked = false;

    @Column(name = "temporarily_locked", nullable = false)
    private boolean temporarilyLocked = false;

    @Column(name = "lockout_end")
    private LocalDateTime lockoutEnd;

    @Version
    private Long version;

    public User(String username, String email, String password, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User() {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVerificationCodeExpired() {
        return verificationCodeExpiresAt != null &&
                verificationCodeExpiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isPasswordResetTokenExpired() {
        return passwordResetExpiresAt != null &&
                passwordResetExpiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isCurrentlyLocked() {
        return locked && lockoutEnd != null && lockoutEnd.isAfter(LocalDateTime.now());
    }

    public boolean needsUnlock() {
        return locked && lockoutEnd != null && lockoutEnd.isBefore(LocalDateTime.now());
    }

    public boolean hasExceededFailedAttempts(int maxAttempts) {
        return failedAttempts >= maxAttempts;
    }
}
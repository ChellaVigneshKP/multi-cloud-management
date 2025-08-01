package com.multicloud.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private final Token token;
    private final Failure failure;
    private final Cookie cookie;

    public AuthProperties(Token token, Failure failure, Cookie cookie) {
        this.token = token;
        this.failure = failure;
        this.cookie = cookie;
    }

    public record Token(
            @Min(1) int expiryNormalDays,
            @Min(1) int expiryRememberDays) {

    }

    public record Failure(
            @Min(1) int maxAttempts,
            @Min(1) int deviceMaxAttempts,
            @Min(1) int globalMaxAttempts,
            @Min(1) int lockoutWindowHours,
            @Min(1) int lockoutDurationMinutes) {

    }

    public record Cookie(@NotBlank String sameSite) {

    }
}

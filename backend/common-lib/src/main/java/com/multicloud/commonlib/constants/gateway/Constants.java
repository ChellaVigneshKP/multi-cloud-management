package com.multicloud.commonlib.constants.gateway;

import java.util.List;

/**
 * Constants.java
 * This class defines constants used in the gateway module.
 * It is designed to be a utility class and should not be instantiated.
 */
public class Constants {
    /**
     * Private constructor to prevent instantiation.
     * This class is a utility class and should not be instantiated.
     */
    private Constants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    /**
     * List of open API endpoints that do not require authentication.
     * These endpoints are accessible without any security checks.
     */
    public static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/auth/signup",
            "/eureka",
            "/auth/verify",
            "/auth/login",
            "/auth/resend",
            "/auth/validate-token",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/take-action",
            "/auth/v3/**",
            "/auth/swagger-ui/**",
            "/favicon.ico",
            "/auth/refresh-token",
            "/auth/logout",
            "/csrf"
    );
}

package com.multicloud.auth.util;

import org.springframework.http.ResponseCookie;
import java.time.Duration;

public class CookieUtil {

    // Private constructor to prevent instantiation
    private CookieUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static ResponseCookie createCookie(String name, String value, Duration maxAge, boolean httpOnly, boolean secure, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }
    public static ResponseCookie createCookie(String name, String value, long maxAge, boolean httpOnly, boolean secure, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }
}
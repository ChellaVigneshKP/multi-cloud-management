package com.multicloud.commonlib.constants;

/**
 * Utility class containing authentication-related constants.
 */
public class AuthConstants {
    private AuthConstants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    /**
     * The name of the HTTP cookie used for refresh tokens.
     */
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
}

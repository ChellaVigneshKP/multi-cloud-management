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
    /**
     * The name of the HTTP cookie used for JWE tokens.
     */
    public static final String JWE_TOKEN_COOKIE_NAME = "jweToken";

    /**
     * The name of the response to indicate the credentials is invalid.
     */
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
}

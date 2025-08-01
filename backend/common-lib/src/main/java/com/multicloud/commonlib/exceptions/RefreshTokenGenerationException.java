package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when there is an error generating a refresh token.
 * This could be due to various reasons such as database issues, token generation failures, etc.
 */
public class RefreshTokenGenerationException extends RuntimeException {
    /**
     * Constructs a new RefreshTokenGenerationException with the specified detail message.
     *
     * @param message the detail message
     */
    public RefreshTokenGenerationException(String message) {
        super(message);
    }
}

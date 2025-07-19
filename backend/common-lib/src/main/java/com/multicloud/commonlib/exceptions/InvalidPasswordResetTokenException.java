package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when a password-reset token is invalid.
 * This could be due to the token being expired, malformed, or not found.
 */
public class InvalidPasswordResetTokenException extends RuntimeException {
    /**
     * Constructs a new InvalidPasswordResetTokenException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidPasswordResetTokenException(String message) {
        super(message);
    }
}
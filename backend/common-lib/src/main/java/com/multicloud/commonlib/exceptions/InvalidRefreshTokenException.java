package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when a refresh token is invalid.
 * This could be due to expiration, tampering, or other issues.
 */
public class InvalidRefreshTokenException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(InvalidRefreshTokenException.class);

    /**
     * Constructs a new InvalidRefreshTokenException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidRefreshTokenException(String message) {
        super(message);
        logger.error(message);
    }

    /**
     * Constructs a new InvalidRefreshTokenException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
        logger.error(message, cause);
    }
}
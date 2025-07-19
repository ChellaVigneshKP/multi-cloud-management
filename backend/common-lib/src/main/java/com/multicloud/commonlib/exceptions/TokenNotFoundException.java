package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when a token is not found in the system.
 * This could be due to various reasons such as expired tokens, invalid tokens, or tokens that do not exist.
 */
public class TokenNotFoundException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(TokenNotFoundException.class);
    /**
     * Default constructor for TokenNotFoundException.
     * Initializes the exception with a default message.
     * @param message the detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public TokenNotFoundException(String message) {
        super(message);
        // Log the error message when the exception is created
        logger.error("Token not found exception: {}", message);
    }
}

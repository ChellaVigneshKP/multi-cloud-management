package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when a user is not found in the system.
 * This exception is typically used in authentication and user management scenarios.
 */
public class UsernameNotFoundException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(UsernameNotFoundException.class);
    /**
     * Constructs a new UsernameNotFoundException with the specified detail message.
     *
     * @param message the detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public UsernameNotFoundException(String message) {
        super(message);
        logger.error("User not found");
    }
}

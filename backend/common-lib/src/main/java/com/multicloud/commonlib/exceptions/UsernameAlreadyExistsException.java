package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when a username already exists in the system.
 * This is a runtime exception, meaning it does not need to be declared in method signatures.
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(UsernameAlreadyExistsException.class);
    /**
     * Default constructor for UsernameAlreadyExistsException.
     * Logs an error message indicating that the username already exists.
     * @param message the detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public UsernameAlreadyExistsException(String message) {
        super(message);
        logger.error("Username already exists");
    }
}

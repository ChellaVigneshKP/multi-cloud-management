package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when an email is already registered.
 * This exception is used to indicate that the user is trying to register with an email that is already in use.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(EmailAlreadyRegisteredException.class);
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message explaining the error
     */
    public EmailAlreadyRegisteredException(String message) {
        super(message);
        logger.error("Email already registered");
    }
}

package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when attempting to verify an account that is already verified.
 */
public class AccountAlreadyVerifiedException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(AccountAlreadyVerifiedException.class);
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message explaining the error
     */
    public AccountAlreadyVerifiedException(String message) {
        super(message);
        logger.error(message);
    }
}
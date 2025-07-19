package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when an account is not verified.
 * This exception is used to indicate that the user needs to verify their account before proceeding.
 */
public class AccountNotVerifiedException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(AccountNotVerifiedException.class);
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message explaining the error
     */
    public AccountNotVerifiedException(String message) {
        super(message);
        logger.error("Account not verified");
    }
}

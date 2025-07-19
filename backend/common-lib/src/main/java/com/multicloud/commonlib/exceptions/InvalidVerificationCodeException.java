package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when an invalid verification code is encountered.
 * This could be due to an expired, incorrect, or already used verification code.
 */
public class InvalidVerificationCodeException extends  RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(InvalidVerificationCodeException.class);
    /**
     * Constructs a new InvalidVerificationCodeException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidVerificationCodeException(String message) {
        super(message);
        logger.error(message);
    }
}
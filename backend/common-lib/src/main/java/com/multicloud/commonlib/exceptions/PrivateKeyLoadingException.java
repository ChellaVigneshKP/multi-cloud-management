package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when there is an error loading a private key.
 * This could be due to issues such as file not found, incorrect format, or decryption failure.
 */
public class PrivateKeyLoadingException extends Exception {
    private static final Logger logger = LoggerFactory.getLogger(PrivateKeyLoadingException.class);
    /**
     * Constructs a new PrivateKeyLoadingException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public PrivateKeyLoadingException(String message, Throwable cause) {
        super(message, cause);
        logger.error("PrivateKeyLoadingException : {}",message);
    }
}

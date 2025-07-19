package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when there is an error during JWE (JSON Web Encryption) token encryption.
 * This exception extends RuntimeException and logs the error message using SLF4J.
 */
public class JweEncryptionException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(JweEncryptionException.class);
    /**
     * Constructs a new JweEncryptionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public JweEncryptionException(String message, Throwable cause) {
        super(message, cause);
        logger.error("Error while building JWE token : {}",message);
    }

    /**
     * Constructs a new JweEncryptionException with the specified detail message.
     *
     * @param message the detail message
     */

    public JweEncryptionException(String message) {
        super(message);
        logger.error("Error while building JWE token");
    }
}

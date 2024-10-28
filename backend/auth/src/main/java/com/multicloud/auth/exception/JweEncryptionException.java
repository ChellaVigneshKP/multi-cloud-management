package com.multicloud.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JweEncryptionException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(JweEncryptionException.class);
    public JweEncryptionException(String message, Throwable cause) {
        super(message, cause);
        logger.error("Error while building JWE token : {}",message);
    }

    public JweEncryptionException(String message) {
        super(message);
        logger.error("Error while building JWE token");
    }
}

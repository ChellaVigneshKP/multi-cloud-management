package com.multicloud.api_gateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateKeyLoadingException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(PrivateKeyLoadingException.class);
    public PrivateKeyLoadingException(String message) {
        super(message);
        logger.error(message);
    }

    public PrivateKeyLoadingException(String message, Throwable cause) {
        super(message, cause);
        logger.error(message, cause);
    }
}
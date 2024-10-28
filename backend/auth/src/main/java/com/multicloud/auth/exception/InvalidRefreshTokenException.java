package com.multicloud.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvalidRefreshTokenException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(InvalidRefreshTokenException.class);

    public InvalidRefreshTokenException(String message) {
        super(message);
        logger.error(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
        logger.error(message, cause);
    }
}
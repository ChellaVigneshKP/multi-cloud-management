package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenNotFoundException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(TokenNotFoundException.class);

    public TokenNotFoundException(String message) {
        super(message);
        // Log the error message when the exception is created
        logger.error("Token not found exception: {}", message);
    }
}

package com.multicloud.api_gateway.exception;

import org.slf4j.Logger;

public class TokenExpiredException extends Exception {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TokenExpiredException.class);
    public TokenExpiredException(String message) {
        super(message);
        logger.error(message);
    }
}

package com.multicloud.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvalidVerificationCodeException extends  RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(InvalidVerificationCodeException.class);
    public InvalidVerificationCodeException(String message) {
        super(message);
        logger.error(message);
    }
}
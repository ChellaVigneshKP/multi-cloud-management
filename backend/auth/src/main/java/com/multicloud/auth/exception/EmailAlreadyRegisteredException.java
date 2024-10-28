package com.multicloud.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailAlreadyRegisteredException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(EmailAlreadyRegisteredException.class);
    public EmailAlreadyRegisteredException(String message) {
        super(message);
        logger.error("Email already registered");
    }
}

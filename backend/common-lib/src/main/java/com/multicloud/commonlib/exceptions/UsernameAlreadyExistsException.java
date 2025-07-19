package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsernameAlreadyExistsException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(UsernameAlreadyExistsException.class);
    public UsernameAlreadyExistsException(String message) {
        super(message);
        logger.error("Username already exists");
    }
}

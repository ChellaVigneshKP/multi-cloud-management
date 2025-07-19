package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsernameNotFoundException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(UsernameNotFoundException.class);
    public UsernameNotFoundException(String message) {
        super(message);
        logger.error("User not found");
    }
}

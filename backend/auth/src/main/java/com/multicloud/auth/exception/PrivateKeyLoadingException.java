package com.multicloud.auth.exception;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class PrivateKeyLoadingException extends Exception {
    private static final Logger logger = LoggerFactory.getLogger(PrivateKeyLoadingException.class);
    public PrivateKeyLoadingException(String message, Throwable cause) {
        super(message, cause);
        logger.error("PrivateKeyLoadingException : {}",message);
    }
}

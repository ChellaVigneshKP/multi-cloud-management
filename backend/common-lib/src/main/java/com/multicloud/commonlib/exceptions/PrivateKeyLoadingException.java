package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateKeyLoadingException extends Exception {
    private static final Logger logger = LoggerFactory.getLogger(PrivateKeyLoadingException.class);
    public PrivateKeyLoadingException(String message, Throwable cause) {
        super(message, cause);
        logger.error("PrivateKeyLoadingException : {}",message);
    }
}

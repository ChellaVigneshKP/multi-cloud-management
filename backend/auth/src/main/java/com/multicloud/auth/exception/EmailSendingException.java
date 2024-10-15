package com.multicloud.auth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailSendingException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(EmailSendingException.class);
    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
        logger.error("Failed to Send Email");
    }
}
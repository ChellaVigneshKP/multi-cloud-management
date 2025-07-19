package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotificationPublishException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationPublishException.class);
    public EmailNotificationPublishException(String message) {
        super(message);
    }

    public EmailNotificationPublishException(String message, Throwable cause) {
        super(message, cause);
        logger.error("Failed to Send Email");
    }
}
package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception thrown when there is an error in publishing an email notification.
 * This could be due to various reasons such as network issues, invalid email format,
 * or problems with the email service provider.
 */
public class EmailNotificationPublishException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationPublishException.class);
    /**
     * Constructs a new EmailNotificationPublishException with no detail message.
     * @param message the detail message
     */
    public EmailNotificationPublishException(String message) {
        super(message);
    }
    /**
     * Constructs a new EmailNotificationPublishException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public EmailNotificationPublishException(String message, Throwable cause) {
        super(message, cause);
        logger.error("Failed to Send Email");
    }
}
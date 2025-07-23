package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when there is an authentication error in the CCloud service.
 * This is a runtime exception that can be used to indicate issues such as invalid credentials,
 * account not verified, or other authentication-related problems.
 */
public class CCloudAuthException extends RuntimeException {
    /**
     * Default constructor for CCloudAuthException.
     * Initializes the exception with no message.
     * @param message the detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public CCloudAuthException(String message) {
        super(message);
    }
}

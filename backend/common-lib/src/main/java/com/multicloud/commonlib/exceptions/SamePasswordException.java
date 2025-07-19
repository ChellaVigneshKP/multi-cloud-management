package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when a user attempts to set a new password that is the same as the current password.
 */
public class SamePasswordException extends RuntimeException {
    /**
     * Constructs a SamePasswordException with the specified detail message.
     *
     * @param message the detail message
     */
    public SamePasswordException(String message) {
        super(message);
    }
}


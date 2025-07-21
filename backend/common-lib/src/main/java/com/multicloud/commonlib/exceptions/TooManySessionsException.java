package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when a user exceeds the maximum number of allowed sessions.
 * This is typically used in scenarios where session management is enforced,
 * such as limiting concurrent logins for a user.
 */
public class TooManySessionsException extends RuntimeException {
    /**
     * Constructs a new TooManySessionsException with the specified detail message.
     *
     * @param message the detail message
     */
    public TooManySessionsException(String message) {
        super(message);
    }
}

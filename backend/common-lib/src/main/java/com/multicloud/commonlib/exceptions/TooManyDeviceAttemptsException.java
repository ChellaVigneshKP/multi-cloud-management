package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when a user exceeds the maximum number of allowed attempts to access a device.
 * This is typically used to prevent brute-force attacks or excessive login attempts.
 */
public class TooManyDeviceAttemptsException extends RuntimeException {
    /**
     * Constructs a new TooManyDeviceAttemptsException with the specified detail message.
     *
     * @param message the detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public TooManyDeviceAttemptsException(String message) {
        super(message);
    }
}

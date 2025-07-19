package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when there is an error during JWE decryption.
 */
public class JweDecryptionException extends RuntimeException {
    /**
     * Constructs a new JweDecryptionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public JweDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * Constructs a new JweDecryptionException with the specified detail message.
     *
     * @param message the detail message
     */
    public JweDecryptionException(String message) {
        super(message);
    }
}

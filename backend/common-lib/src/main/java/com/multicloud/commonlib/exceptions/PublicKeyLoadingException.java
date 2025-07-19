package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when there is an error loading a public key.
 */
public class PublicKeyLoadingException extends Exception {
    /**
     * Constructs a new PublicKeyLoadingException with the specified detail message.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public PublicKeyLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.multicloud.auth.exception;

public class JweDecryptionException extends RuntimeException {
    public JweDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public JweDecryptionException(String message) {
        super(message);
    }
}
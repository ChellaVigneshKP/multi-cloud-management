package com.multicloud.commonlib.exceptions;

/**
 * Exception thrown when a user account is locked.
 * This typically occurs after multiple failed login attempts or due to administrative actions.
 */
public class AccountLockedException extends RuntimeException {
    /**
     * Constructs a new AccountLockedException with the specified detail message.
     *
     * @param message the detail message
     */
    public AccountLockedException(String message) {
        super(message);
    }
}

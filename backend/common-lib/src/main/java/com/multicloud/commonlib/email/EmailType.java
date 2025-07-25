package com.multicloud.commonlib.email;

/**
 * Enum representing various types of email notifications.
 */
public enum EmailType {
    /**
     * Email sent when a user resets their password.
     */
    PASSWORD_RESET,
    /**
     * Email sent for user verification.
     */
    VERIFICATION,
    /**
     * Email sent when a login alert is triggered.
     */
    LOGIN_ALERT,
    /**
     * Email sent when a suspicious login is detected.
     */
    SUSPICIOUS_LOGIN_ALERT,
    /**
     * Email sent when an account is locked due to suspicious activity.
     */
    ACCOUNT_LOCKED_ALERT,
}
package com.multicloud.commonlib.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Email request object for password reset emails.
 */
@Getter
@Setter
@AllArgsConstructor
public class PasswordResetEmailRequest extends EmailRequest {
    private String firstName;
    private String resetLink;
    private String logoUrl;

    /**
     * Default constructor for PasswordResetEmailRequest.
     * Initializes the email type to PASSWORD_RESET.
     */
    public PasswordResetEmailRequest() {}
}

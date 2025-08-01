package com.multicloud.commonlib.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.context.Context;

/**
 * Email request object for password reset emails.
 */
@Getter
@Setter
@AllArgsConstructor
public class PasswordResetEmailRequest extends EmailRequest {
    private String firstName;
    private String resetLink;

    /**
     * Default constructor for PasswordResetEmailRequest.
     * Initializes the email type to PASSWORD_RESET.
     */
    public PasswordResetEmailRequest() {
        super();
        this.setType(EmailType.PASSWORD_RESET);
    }

    @Override
    public void populateContext(Context context) {
        context.setVariable("firstName", firstName);
        context.setVariable("resetLink", resetLink);
        context.setVariable("logo", getLogoUrl());
    }
}

package com.multicloud.commonlib.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.context.Context;

/**
 * Email request object for verification emails.
 */
@Getter
@Setter
@AllArgsConstructor
public class VerificationEmailRequest extends EmailRequest {
    private String firstName;
    private String verificationCode;

    /**
     * Default constructor for VerificationEmailRequest.
     * Initializes the email type to VERIFICATION.
     */
    public VerificationEmailRequest() {
        super();
        this.setType(EmailType.VERIFICATION);
    }

    @Override
    public void populateContext(Context context) {
        context.setVariable("firstName", firstName);
        context.setVariable("verificationCode", verificationCode);
        context.setVariable("logo", getLogoUrl());
    }
}

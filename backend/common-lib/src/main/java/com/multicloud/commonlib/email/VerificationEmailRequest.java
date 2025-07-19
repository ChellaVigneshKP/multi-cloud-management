package com.multicloud.commonlib.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Email request object for verification emails.
 */
@Getter
@Setter
@AllArgsConstructor
public class VerificationEmailRequest extends EmailRequest {
    private String firstName;
    private String verificationCode;
    private String logoUrl;

    /**
     * Default constructor for VerificationEmailRequest.
     * Initializes the email type to VERIFICATION.
     */
    public VerificationEmailRequest() {
        super();
        this.setType(EmailType.VERIFICATION);
    }
}

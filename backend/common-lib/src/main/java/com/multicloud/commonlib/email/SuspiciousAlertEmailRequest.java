package com.multicloud.commonlib.email;

import com.multicloud.commonlib.email.dto.SimpleLoginAttemptDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.context.Context;

import java.util.List;

/**
 * Email request object for suspicious login alerts.
 * This class extends EmailRequest and is used to send notifications
 * when a suspicious login is detected.
 */
@Getter
@Setter
@AllArgsConstructor
public class SuspiciousAlertEmailRequest extends EmailRequest{
    private String firstName;
    private String changePasswordUrl;
    private List<SimpleLoginAttemptDTO> attempts;
    /**
     * Default constructor for SuspiciousAlertEmailRequest.
     * Initializes the email type to SUSPICIOUS_LOGIN_ALERT.
     */
    public SuspiciousAlertEmailRequest() {
        super();
        this.setType(EmailType.SUSPICIOUS_LOGIN_ALERT);
    }

    @Override
    public void populateContext(Context context) {
        context.setVariable("firstName", firstName);
        context.setVariable("changePasswordUrl", changePasswordUrl);
        context.setVariable("attempts", attempts);
        context.setVariable("logo", getLogoUrl());
    }
}

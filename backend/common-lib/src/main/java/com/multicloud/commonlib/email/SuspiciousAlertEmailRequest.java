package com.multicloud.commonlib.email;

import com.multicloud.commonlib.email.dto.SimpleLoginAttemptDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
    private String logoUrl;
    /**
     * Default constructor for SuspiciousAlertEmailRequest.
     * Initializes the email type to SUSPICIOUS_LOGIN_ALERT.
     */
    public SuspiciousAlertEmailRequest() {
        super();
        this.setType(EmailType.SUSPICIOUS_LOGIN_ALERT);
    }
}

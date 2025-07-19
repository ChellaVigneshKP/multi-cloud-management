package com.multicloud.commonlib.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Email request object for login alerts.
 */
@Getter
@Setter
@AllArgsConstructor
public class LoginAlertEmailRequest extends EmailRequest {
    private String username;
    private String os;
    private String browser;
    private String location;
    private String country;
    private String formattedLoginTime;
    private String clientIp;
    private String mapUrl;
    private String changePasswordUrl;
    private String deviceImagePath;
    private String logoUrl;

    /**
     * Default constructor for LoginAlertEmailRequest.
     * Initializes the email type to LOGIN_ALERT.
     */
    public LoginAlertEmailRequest() {
        super();
        this.setType(EmailType.LOGIN_ALERT);
    }
}

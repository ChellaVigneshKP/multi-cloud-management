package com.multicloud.commonlib.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.context.Context;

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

    /**
     * Default constructor for LoginAlertEmailRequest.
     * Initializes the email type to LOGIN_ALERT.
     */
    public LoginAlertEmailRequest() {
        super();
        this.setType(EmailType.LOGIN_ALERT);
    }

    @Override
    public void populateContext(Context context) {
        context.setVariable("logo", getLogoUrl());
        context.setVariable("username", username);
        context.setVariable("os", os);
        context.setVariable("browser", browser);
        context.setVariable("location", location);
        context.setVariable("country", country);
        context.setVariable("formattedLoginTime", formattedLoginTime);
        context.setVariable("clientIp", clientIp);
        context.setVariable("mapUrl", mapUrl);
        context.setVariable("changePasswordUrl", changePasswordUrl);
        context.setVariable("deviceImagePath", deviceImagePath);
    }
}

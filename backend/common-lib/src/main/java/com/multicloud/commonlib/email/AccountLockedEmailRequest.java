package com.multicloud.commonlib.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.context.Context;

/**
 * Email request object for account locked notifications.
 * This class extends EmailRequest and is used to send notifications when an account is locked due to suspicious activity.
 */
@Getter
@Setter
@AllArgsConstructor
public class AccountLockedEmailRequest extends EmailRequest {
    private String firstName;
    private String clientIp;
    private String location;
    private String lockTime;
    private String unlockSupportLink;
    public AccountLockedEmailRequest() {
        super();
        this.setType(EmailType.ACCOUNT_LOCKED_ALERT);
    }

    @Override
    public void populateContext(Context context) {
        context.setVariable("logo", getLogoUrl());
        context.setVariable("firstName", firstName);
        context.setVariable("clientIp", clientIp);
        context.setVariable("location", location);
        context.setVariable("lockTime", lockTime);
        context.setVariable("unlockSupportLink", unlockSupportLink);
    }
}

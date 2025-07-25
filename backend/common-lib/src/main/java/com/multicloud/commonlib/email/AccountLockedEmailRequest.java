package com.multicloud.commonlib.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Email request object for account locked notifications.
 * This class extends EmailRequest and is used to send notifications when an account is locked due to suspicious activity.
 */
@Getter
@Setter
@AllArgsConstructor
public class AccountLockedEmailRequest extends EmailRequest{
    private String logoUrl;
    public AccountLockedEmailRequest() {
        super();
        this.setType(EmailType.ACCOUNT_LOCKED_ALERT);
    }
}

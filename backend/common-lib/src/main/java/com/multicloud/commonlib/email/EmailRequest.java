package com.multicloud.commonlib.email;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a generic email request.
 * This is the base class for all email request types.
 */
@Getter
@Setter
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PasswordResetEmailRequest.class, name = "PASSWORD_RESET"),
        @JsonSubTypes.Type(value = LoginAlertEmailRequest.class, name = "LOGIN_ALERT"),
        @JsonSubTypes.Type(value = VerificationEmailRequest.class, name = "VERIFICATION"),
        @JsonSubTypes.Type(value = SuspiciousAlertEmailRequest.class, name = "SUSPICIOUS_LOGIN_ALERT"),
        @JsonSubTypes.Type(value = AccountLockedEmailRequest.class, name = "ACCOUNT_LOCKED")
})
public abstract class EmailRequest {
    private String to;
    private String subject;
    private EmailType type;

    /**
     * Default constructor for EmailRequest.
     */
    protected EmailRequest() {
        // Default constructor for deserialization
    }
}

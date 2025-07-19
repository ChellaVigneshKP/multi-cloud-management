package com.multicloud.commonlib.email;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an email notification that includes an email request and a template name.
 */
@Getter
@Setter
@AllArgsConstructor
public class EmailNotification {
    private EmailRequest emailRequest;
    private String templateName;

    /**
     * Default constructor for EmailNotification.
     * Initializes the email request to null and template name to an empty string.
     */
    public EmailNotification() {}
}

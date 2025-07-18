package com.multicloud.commonlib.email;

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
public abstract class EmailRequest {
    private String to;
    private String subject;
    private EmailType type;

    /**
     * Default constructor for EmailRequest.
     */
    public EmailRequest() {
    }
}

package com.multicloud.commonlib.email.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for simple login attempt details.
 * This class is used to encapsulate the information about a login attempt
 * including the email, IP address, user agent, and the time of the attempt.
 */
@Getter
@Setter
@AllArgsConstructor
public class SimpleLoginAttemptDTO {
    private String email;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime attemptTime;
    /**
     * Default constructor for SimpleLoginAttemptDTO.
     * Initializes a new instance without any parameters.
     */
    public SimpleLoginAttemptDTO() {
        // Default constructor
    }
}
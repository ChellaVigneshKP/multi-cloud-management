package com.multicloud.auth.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LoginProcessParameters {
    private LoginUserDto loginRequest;
    private String userAgent;
    private HttpServletRequest request;
    private String clientIp;
    private LocalDateTime now;
    private long failedAttemptsFromDevice;
    private long uniqueVisitorIdFailures;
}

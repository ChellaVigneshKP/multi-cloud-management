package com.multicloud.auth.dto;

import com.multicloud.auth.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class LoginAlertDto {
    private User user;
    private String clientIp;
    private String userAgent;
    private String city;
    private String region;
    private String country;
    private ZonedDateTime lastLogin;
    private String mapUrl;
    private String formattedLoginTime;
    private String deviceImagePath;
    private String os;
    private String browser;
}
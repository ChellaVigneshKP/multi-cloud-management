package com.multicloud.auth.dto;

import com.multicloud.auth.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginAlertDto {
    private User user;
    private String clientIp;
    private String userAgent;
    public LoginAlertDto(User user, String clientIp, String userAgent) {
        this.user = user;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }
}

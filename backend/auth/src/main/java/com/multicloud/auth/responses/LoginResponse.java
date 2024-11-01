package com.multicloud.auth.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String jwtToken;
    private long expiresIn;
    private String refreshToken;
    private long refreshTokenExpiry;
    public LoginResponse(String token, long expiresIn) {
        this.jwtToken = token;
        this.expiresIn = expiresIn;
    }
}

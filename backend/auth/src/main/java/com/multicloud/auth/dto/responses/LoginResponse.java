package com.multicloud.auth.dto.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String message;  // Optional, for confirmation messages
    public LoginResponse(String message) {
        this.message = message;
    }
}
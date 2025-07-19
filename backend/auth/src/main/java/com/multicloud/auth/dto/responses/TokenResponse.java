package com.multicloud.auth.dto.responses;
import com.fasterxml.jackson.annotation.JsonView;
import com.multicloud.auth.view.Views;

public record TokenResponse(
        @JsonView(Views.Public.class) String accessToken,
        String refreshToken,
        long refreshTokenExpiry) {
}
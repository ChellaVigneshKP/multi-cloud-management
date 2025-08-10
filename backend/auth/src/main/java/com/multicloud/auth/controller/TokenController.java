package com.multicloud.auth.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.multicloud.auth.dto.responses.ErrorResponse;
import com.multicloud.auth.dto.responses.LoginResponse;
import com.multicloud.auth.dto.responses.TokenResponse;
import com.multicloud.auth.entity.RefreshToken;
import com.multicloud.auth.entity.User;
import com.multicloud.auth.repository.RefreshTokenRepository;
import com.multicloud.auth.service.AuthenticationService;
import com.multicloud.auth.service.ForgotPasswordService;
import com.multicloud.auth.service.JweService;
import com.multicloud.auth.util.CookieUtil;
import com.multicloud.auth.view.Views;
import com.multicloud.commonlib.exceptions.InvalidRefreshTokenException;
import com.multicloud.commonlib.exceptions.TokenNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;

@RequestMapping("/auth")
@RestController
@Tag(name = "TokenAction", description = "Endpoints for login, logout")
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private final JweService jweService;
    private final AuthenticationService authenticationService;  // Service for authentication-related tasks
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private final ForgotPasswordService forgotPasswordService;
    private final HttpServletRequest request;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenController(JweService jweService,
                           AuthenticationService authenticationService,
                           ForgotPasswordService forgotPasswordService,
                           HttpServletRequest request,
                           RefreshTokenRepository refreshTokenRepository) {
        this.jweService = jweService;
        this.authenticationService = authenticationService;
        this.forgotPasswordService = forgotPasswordService;
        this.request = request;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Operation(summary = "Refresh JWT token", description = "Generate new JWT and refresh tokens using an existing refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - refresh token is required",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - refresh token is invalid or expired",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh-token")
    @JsonView(Views.Public.class)
    public ResponseEntity<Object> refreshToken(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing");
        }
        try{
            logger.debug("Received refresh token: {}", refreshToken);
            RefreshToken currentRefreshToken = authenticationService.getRefreshToken(refreshToken);
            User user = currentRefreshToken.getUser();
            TokenResponse tokens = authenticationService.refreshTokens(user, refreshToken);
            Duration remainingDuration = Duration.between(LocalDateTime.now(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(tokens.refreshTokenExpiry()), ZoneId.systemDefault()));
            long maxAgeSeconds = remainingDuration.getSeconds();
            ResponseCookie refreshTokenCookie = CookieUtil
                    .createCookie(REFRESH_TOKEN_COOKIE_NAME,
                            tokens.refreshToken(),
                            maxAgeSeconds, true, true,
                            "None");
            ResponseCookie jweTokenCookie = CookieUtil
                    .createCookie("jweToken",
                            tokens.accessToken(),
                            Duration.ofMillis(jweService.getExpirationTime()), true, true,
                            "None");
            ResponseCookie isAuthenticated = CookieUtil.createCookie("isAuthenticated", "true", maxAgeSeconds, false, true, "None");
            logger.info("Token refreshed successfully for User: {}", user.getUsername());
            LoginResponse loginResponse = new LoginResponse(null,"Token refreshed successfully", null,null);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, jweTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, isAuthenticated.toString())
                    .body(loginResponse);
        } catch (InvalidRefreshTokenException | TokenNotFoundException e) {
            ResponseCookie expiredCookie = CookieUtil.createCookie(REFRESH_TOKEN_COOKIE_NAME, "", 0, true, true, "None");
            logger.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                    .body("Invalid or expired refresh token");
        }
    }

    @Operation(summary = "Validate JWT token", description = "Validate the provided JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/validate-token")
    public ResponseEntity<Object> validateToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token == null || token.isEmpty()) {
            logger.error("Token is required");
            return ResponseEntity.badRequest().body(new ErrorResponse("Token is required"));
        }

        try {
            String username = jweService.extractUsername(token);
            User user = authenticationService.loadUserByUsername(username);
            if (jweService.isTokenValid(token, user)) {
                logger.info("Token is Valid for User: {}", user.getUsername());
                return ResponseEntity.ok(Collections.singletonMap("message", "Token is valid"));
            } else {
                logger.error("Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid or expired token"));
            }
        } catch (Exception e) {
            logger.error("Invalid or malformed token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid or malformed token"));
        }
    }

}

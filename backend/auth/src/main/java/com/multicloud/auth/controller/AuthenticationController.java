package com.multicloud.auth.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.multicloud.auth.dto.*;
import com.multicloud.auth.exception.*;
import com.multicloud.auth.model.RefreshToken;
import com.multicloud.auth.model.User;
import com.multicloud.auth.repository.RefreshTokenRepository;
import com.multicloud.auth.responses.LoginResponse;
import com.multicloud.auth.responses.TokenResponse;
import com.multicloud.auth.service.AuthenticationService;
import com.multicloud.auth.service.ForgotPasswordService;
import com.multicloud.auth.service.JweService;
import com.multicloud.auth.util.CookieUtil;
import com.multicloud.auth.view.Views;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import com.multicloud.auth.responses.ErrorResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/auth")  // Base URL for authentication-related endpoints
@RestController  // Indicates that this class serves RESTful web services
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final JweService jweService;
    private final AuthenticationService authenticationService;  // Service for authentication-related tasks
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private final ForgotPasswordService forgotPasswordService;
    private final HttpServletRequest request;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthenticationController(
            JweService jweService,
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

    @Operation(summary = "User registration", description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Username or Email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<Object> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            authenticationService.signup(registerUserDto);
            logger.info("User Registered Successfully with Username: {}", registerUserDto.getUsername());
            return ResponseEntity.ok("Please Verify Your email: " + registerUserDto.getEmail());
        } catch (UsernameAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already exists"));
        } catch (EmailAlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email already registered"));
        }
    }

    @Operation(summary = "User login", description = "Authenticate an existing user and generate a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Account not verified",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<Object> authenticate(@RequestBody LoginUserDto loginUserDto, @RequestHeader("User-Agent") String userAgent) {
        try {
            final String UNKNOWN_IP = "Unknown";  // Define a constant for "Unknown"
            String clientIpv4 = request.getHeader("X-User-IP");
            String clientIpv6 = request.getHeader("X-User-IP-V6");
            String clientIp = UNKNOWN_IP;
            if (!UNKNOWN_IP.equals(clientIpv4)) {
                clientIp = clientIpv4;  // Prefer IPv4 if it's available and not "Unknown"
            } else if (!UNKNOWN_IP.equals(clientIpv6)) {
                clientIp = clientIpv6;  // Fallback to IPv6 if IPv4 is "Unknown"
            }
            User authenticatedUser = authenticationService.authenticate(loginUserDto, userAgent, clientIp);
            String jweToken = jweService.generateToken(authenticatedUser);
            Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByUserAndVisitorId(authenticatedUser, loginUserDto.getVisitorId());
            if (refreshTokenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Refresh token generation failed"));
            }
            String refreshToken = refreshTokenOpt.get().getToken();
            Duration refreshTokenExpiry = loginUserDto.isRemember() ? Duration.ofDays(30) : Duration.ofDays(7);
            ResponseCookie refreshTokenCookie = CookieUtil.createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTokenExpiry, true, true, "None");
            ResponseCookie jweTokenCookie = CookieUtil.createCookie("jweToken", jweToken, Duration.ofMillis(jweService.getExpirationTime()), true, true, "None");
            ResponseCookie isAuthenticated = CookieUtil.createCookie("isAuthenticated", "true", refreshTokenExpiry, false, true, "None");
            logger.info("User Logged In Successfully with Email ID: {}", loginUserDto.getEmail());
            LoginResponse loginResponse = new LoginResponse("Login successful");
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, jweTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, isAuthenticated.toString())
                    .body(loginResponse);
        } catch (UsernameNotFoundException e) {
            logger.error("User not found: {}", loginUserDto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (AccountNotVerifiedException e) {
            logger.error("Account not verified for user: {}", loginUserDto.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", loginUserDto.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            logger.error("An unexpected error occurred during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    @Operation(summary = "Verify user account", description = "Verify a user's email using a verification code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid verification request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/verify")
    public ResponseEntity<Object> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);
            logger.info("User Verified Successfully with Email ID: {}", verifyUserDto.getEmail());
            return ResponseEntity.ok("Account verified successfully");
        } catch (RuntimeException e) {
            logger.error("Bad Request for User Verification");
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @Operation(summary = "Resend verification code", description = "Resend the email verification code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification code resent successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/resend")
    public ResponseEntity<Object> resendVerificationCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            authenticationService.resendVerificationCode(email);
            logger.debug("Verification code sent to {}", email);
            return ResponseEntity.ok("Verification code sent");
        } catch (RuntimeException e) {
            logger.error("Bad Request");
            return ResponseEntity.badRequest().body(e.getMessage());
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

    @Operation(summary = "Get user info", description = "Retrieve user info from the request headers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User information missing",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/userinfo")
    public ResponseEntity<Object> getUserInfo(
            @RequestHeader("X-User-Name") String username,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") String userId) {
        if (username == null || email == null || userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("User information is missing in the request headers"));
        }
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("email", email);
        userInfo.put("userId", userId);
        logger.info("UserInfo requested for User ID: {}", userId);
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "Forgot password", description = "Request password reset link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset link sent successfully")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Received forgot password request for email: {}", request.getEmail());
        try {
            forgotPasswordService.processForgotPassword(request.getEmail());
            return ResponseEntity.ok(new SuccessResponse("A reset link has been sent to email " + request.getEmail() + ". If the email is registered."));
        } catch (IllegalArgumentException e) {
            logger.error("Error processing mail for Forget Password with Mail Id: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse("A reset link has been sent to email " + request.getEmail() + ". If the email is registered."));
        }
    }
    @Operation(summary = "Request a reset password link", description = "Trigger sending of a password reset link for the given email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset link sent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email format",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/take-action")
    public ResponseEntity<Object> takeAction(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Received Change password request for email: {}", request.getEmail());
        try {
            forgotPasswordService.processForgotPassword(request.getEmail());
            return ResponseEntity.ok(new SuccessResponse("A reset link has been sent to your email " + request.getEmail()));
        } catch (IllegalArgumentException e) {
            logger.error("Error processing mail for Change Password with Mail Id: {}", request.getEmail());
            return ResponseEntity.ok(new SuccessResponse("If an account with that email exists, a reset link has been sent."));
        }
    }

    @Operation(summary = "Reset password", description = "Reset user password using a reset token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid password reset token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.debug("Received reset password request");
        try {
            forgotPasswordService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new SuccessResponse("Password has been reset successfully."));
        } catch (InvalidPasswordResetTokenException e) {
            logger.error("Error resetting password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Error resetting password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
            LoginResponse loginResponse = new LoginResponse("Token refreshed successfully");
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
    @Operation(summary = "Logout user", description = "Log out the user by invalidating the refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - refresh token missing",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token is missing");
        }
        authenticationService.logout(refreshToken); // Process logout with the refresh token
        logger.info("User with refresh token {} logged out successfully", refreshToken);

        // Optionally, set the refresh token cookie to expire immediately
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "refreshToken=; Max-Age=0; Path=/; HttpOnly; SameSite=None; Secure");
        headers.add(HttpHeaders.SET_COOKIE, "jweToken=; Max-Age=0; Path=/; HttpOnly; SameSite=None; Secure");
        headers.add(HttpHeaders.SET_COOKIE, "isAuthenticated=; Max-Age=0; Path=/; SameSite=None; Secure");
        return ResponseEntity.ok().headers(headers).body("Logged out successfully");
    }
}

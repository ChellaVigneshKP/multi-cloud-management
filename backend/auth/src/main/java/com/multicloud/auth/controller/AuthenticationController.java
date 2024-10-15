package com.multicloud.auth.controller;

import com.multicloud.auth.dto.*;
import com.multicloud.auth.exception.*;
import com.multicloud.auth.model.User;
import com.multicloud.auth.responses.LoginResponse;
import com.multicloud.auth.service.AuthenticationService;
import com.multicloud.auth.service.ForgotPasswordService;
import com.multicloud.auth.service.JweService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.multicloud.auth.responses.ErrorResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/auth")  // Base URL for authentication-related endpoints
@RestController  // Indicates that this class serves RESTful web services
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final JweService jweService;
    private final AuthenticationService authenticationService;  // Service for authentication-related tasks

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    public AuthenticationController(JweService jweService, AuthenticationService authenticationService) {
        this.jweService = jweService;
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "User registration", description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Username or Email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
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
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto, @RequestHeader("User-Agent") String userAgent) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto, userAgent);
            String jweToken = jweService.generateToken(authenticatedUser);
            LoginResponse loginResponse = new LoginResponse(jweToken, jweService.getExpirationTime());
            logger.info("User Logged In Successfully with Email ID: {}", loginUserDto.getEmail());
            return ResponseEntity.ok(loginResponse);
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
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);
            logger.info("User Verified Successfully with Email ID: {}", verifyUserDto.getEmail());
            return ResponseEntity.ok("Account verified successfully");
        } catch (RuntimeException e) {
            logger.error("Bad Request");
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
    public ResponseEntity<?> resendVerificationCode(@RequestBody Map<String, String> payload) {
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
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> payload) {
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
    public ResponseEntity<?> getUserInfo(
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
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
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
    public ResponseEntity<?> takeAction(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Received Change password request for email: {}", request.getEmail());
        try {
            forgotPasswordService.processForgotPassword(request.getEmail());
            return ResponseEntity.ok(new SuccessResponse("A reset link has been sent to your email " + request.getEmail()));
        } catch (IllegalArgumentException e) {
            logger.error("Error processing mail for Change Password with Mail Id: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse("A reset link has been sent to your email " + request.getEmail()));
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
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
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
}

package com.multicloud.auth.controller;

import com.multicloud.auth.dto.RegisterUserDto;
import com.multicloud.auth.dto.VerifyUserDto;
import com.multicloud.auth.exception.EmailAlreadyRegisteredException;
import com.multicloud.auth.exception.UsernameAlreadyExistsException;
import com.multicloud.auth.repository.RefreshTokenRepository;
import com.multicloud.auth.responses.ErrorResponse;
import com.multicloud.auth.service.AuthenticationService;
import com.multicloud.auth.service.ForgotPasswordService;
import com.multicloud.auth.service.JweService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RequestMapping("/auth")
@RestController
@Tag(name = "Registration", description = "Endpoints for signup, verify and resend")
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final JweService jweService;
    private final AuthenticationService authenticationService;  // Service for authentication-related tasks
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private final ForgotPasswordService forgotPasswordService;
    private final HttpServletRequest request;
    private final RefreshTokenRepository refreshTokenRepository;

    public RegistrationController(JweService jweService,
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
}

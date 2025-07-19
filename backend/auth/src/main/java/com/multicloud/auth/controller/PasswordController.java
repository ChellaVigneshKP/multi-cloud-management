package com.multicloud.auth.controller;

import com.multicloud.auth.dto.ForgotPasswordRequest;
import com.multicloud.auth.dto.ResetPasswordRequest;
import com.multicloud.auth.dto.SuccessResponse;
import com.multicloud.auth.repository.RefreshTokenRepository;
import com.multicloud.auth.dto.responses.ErrorResponse;
import com.multicloud.auth.service.AuthenticationService;
import com.multicloud.auth.service.ForgotPasswordService;
import com.multicloud.auth.service.JweService;
import com.multicloud.commonlib.exceptions.InvalidPasswordResetTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@Tag(name = "Password", description = "Endpoints for forgot-password, reset-password and take-action")
public class PasswordController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);
    private final JweService jweService;
    private final AuthenticationService authenticationService;  // Service for authentication-related tasks
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private final ForgotPasswordService forgotPasswordService;
    private final HttpServletRequest request;
    private final RefreshTokenRepository refreshTokenRepository;

    public PasswordController(JweService jweService,
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
}

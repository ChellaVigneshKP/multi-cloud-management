package com.multicloud.auth.controller;

import com.multicloud.auth.dto.LoginUserDto;
import com.multicloud.auth.dto.RegisterUserDto;
import com.multicloud.auth.dto.VerifyUserDto;
import com.multicloud.auth.exception.AccountNotVerifiedException;
import com.multicloud.auth.exception.EmailAlreadyRegisteredException;
import com.multicloud.auth.exception.UsernameAlreadyExistsException;
import com.multicloud.auth.exception.UsernameNotFoundException;
import com.multicloud.auth.model.User;
import com.multicloud.auth.responses.LoginResponse;
import com.multicloud.auth.service.AuthenticationService;
import com.multicloud.auth.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.multicloud.auth.responses.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RequestMapping("/auth")  // Base URL for authentication-related endpoints
@RestController  // Indicates that this class serves RESTful web services
public class AuthenticationController {
    private final JwtService jwtService;  // Service for handling JWT operations
    private final AuthenticationService authenticationService;  // Service for authentication-related tasks

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    // Endpoint for user registration
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            authenticationService.signup(registerUserDto);  // Attempt to sign up the user
            return ResponseEntity.ok("Please Verify Your email: " + registerUserDto.getEmail());  // Inform user to verify email
        } catch (UsernameAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already exists"));  // Handle existing username
        } catch (EmailAlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email already registered"));  // Handle existing email
        }
    }

    // Endpoint for user login
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);  // Authenticate the user
            String jwtToken = jwtService.generateToken(authenticatedUser);  // Generate JWT token for the authenticated user
            LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());  // Create login response
            return ResponseEntity.ok(loginResponse);  // Return the login response
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));  // Handle user not found
        } catch (AccountNotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));  // Handle unverified account
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));  // Handle unexpected errors
        }
    }

    // Endpoint for verifying user accounts
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);  // Verify the user account
            return ResponseEntity.ok("Account verified successfully");  // Inform user of successful verification
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));  // Handle verification errors
        }
    }

    // Endpoint for resending verification codes
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");  // Extract email from the request payload
        try {
            authenticationService.resendVerificationCode(email);  // Resend the verification code
            return ResponseEntity.ok("Verification code sent");  // Confirm that the code was sent
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());  // Handle errors in resending
        }
    }

    // Endpoint for validating JWT tokens
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");  // Extract token from the request payload

        // Check if token is provided
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Token is required"));  // Return error if token is missing
        }

        try {
            String username = jwtService.extractUsername(token);  // Extract username from the token
            User user = authenticationService.loadUserByUsername(username);  // Load the user by username
            if (jwtService.isTokenValid(token, user)) {  // Validate the token
                return ResponseEntity.ok(Collections.singletonMap("message", "Token is valid"));  // Confirm token validity
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid or expired token"));  // Handle invalid token
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid or malformed token"));  // Handle token errors
        }
    }
}

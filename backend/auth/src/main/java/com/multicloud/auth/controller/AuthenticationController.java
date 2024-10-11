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
import com.multicloud.auth.service.JweService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.multicloud.auth.responses.ErrorResponse;
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

    public AuthenticationController(JweService jweService, AuthenticationService authenticationService) {
        this.jweService = jweService;
        this.authenticationService = authenticationService;
    }

    // Endpoint for user registration
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            authenticationService.signup(registerUserDto);  // Attempt to sign up the user
            logger.info("User Registered Successfully with Username: {}", registerUserDto.getUsername());
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
            String jweToken = jweService.generateToken(authenticatedUser);  // Generate JWT token for the authenticated user
            LoginResponse loginResponse = new LoginResponse(jweToken, jweService.getExpirationTime());  // Create login response
            logger.info("User Logged In Successfully with Email ID: {}", loginUserDto.getEmail());
            return ResponseEntity.ok(loginResponse);  // Return the login response
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));  // Handle user not found
        } catch (AccountNotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));  // Handle unverified account
        } catch (Exception e) {
            logger.error("An unexpected error occurred", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));  // Handle unexpected errors
        }
    }

    // Endpoint for verifying user accounts
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);  // Verify the user account
            logger.info("User Verified Successfully with Email ID: {}", verifyUserDto.getEmail());
            return ResponseEntity.ok("Account verified successfully");  // Inform user of successful verification
        } catch (RuntimeException e) {
            logger.error("Bad Request");
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));  // Handle verification errors
        }
    }

    // Endpoint for resending verification codes
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");  // Extract email from the request payload
        try {
            authenticationService.resendVerificationCode(email);  // Resend the verification code
            logger.debug("Verification code sent to {}", email);
            return ResponseEntity.ok("Verification code sent");  // Confirm that the code was sent
        } catch (RuntimeException e) {
            logger.error("Bad Request");
            return ResponseEntity.badRequest().body(e.getMessage());  // Handle errors in resending
        }
    }

    // Endpoint for validating JWT tokens
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");  // Extract token from the request payload
        if (token == null || token.isEmpty()) {
            logger.error("Token is required");
            return ResponseEntity.badRequest().body(new ErrorResponse("Token is required"));  // Return error if token is missing
        }

        try {
            String username = jweService.extractUsername(token);  // Extract username from the token
            User user = authenticationService.loadUserByUsername(username);  // Load the user by username
            if (jweService.isTokenValid(token, user)) {  // Validate the token
                logger.info("Token is Valid for User: {}",user.getUsername());
                return ResponseEntity.ok(Collections.singletonMap("message", "Token is valid"));  // Confirm token validity
            } else {
                logger.error("Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid or expired token"));  // Handle invalid token
            }
        } catch (Exception e) {
            logger.error("Invalid or malformed token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid or malformed token"));  // Handle token errors
        }
    }

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

}

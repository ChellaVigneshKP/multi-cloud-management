package com.multicloud.auth.controller;

import com.multicloud.auth.dto.LoginUserDto;
import com.multicloud.auth.dto.responses.ErrorResponse;
import com.multicloud.auth.dto.responses.LoginResponse;
import com.multicloud.auth.entity.RefreshToken;
import com.multicloud.auth.entity.User;
import com.multicloud.auth.repository.RefreshTokenRepository;
import com.multicloud.auth.service.AuthenticationService;
import com.multicloud.auth.service.ForgotPasswordService;
import com.multicloud.auth.service.JweService;
import com.multicloud.auth.util.CookieUtil;
import com.multicloud.commonlib.constants.AuthConstants;
import com.multicloud.commonlib.exceptions.AccountNotVerifiedException;
import com.multicloud.commonlib.exceptions.UsernameNotFoundException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;

@RequestMapping("/auth")
@RestController
@Tag(name = "Authentication", description = "Endpoints for login, logout")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final JweService jweService;
    private final AuthenticationService authenticationService;  // Service for authentication-related tasks
    private final HttpServletRequest request;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginController(JweService jweService,
                           AuthenticationService authenticationService,
                           HttpServletRequest request,
                           RefreshTokenRepository refreshTokenRepository) {
        this.jweService = jweService;
        this.authenticationService = authenticationService;
        this.request = request;
        this.refreshTokenRepository = refreshTokenRepository;
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
            ResponseCookie refreshTokenCookie = CookieUtil.createCookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTokenExpiry, true, true, "None");
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


    @Operation(summary = "Logout user", description = "Log out the user by invalidating the refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - refresh token missing",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@CookieValue(value = AuthConstants.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
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

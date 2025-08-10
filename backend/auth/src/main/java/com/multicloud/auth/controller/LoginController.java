package com.multicloud.auth.controller;

import com.multicloud.auth.dto.LoginProcessParameters;
import com.multicloud.auth.dto.LoginUserDto;
import com.multicloud.auth.dto.responses.ErrorResponse;
import com.multicloud.auth.dto.responses.GeneralApiResponse;
import com.multicloud.auth.dto.responses.LoginResponse;
import com.multicloud.auth.service.AuthenticationService;
import com.multicloud.auth.service.auth.LoginService;
import com.multicloud.auth.service.auth.LogoutService;
import com.multicloud.commonlib.constants.AuthConstants;
import io.getunleash.Unleash;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
@Tag(name = "Authentication", description = "Endpoints for login, logout")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final AuthenticationService authenticationService;  // Service for authentication-related tasks
    private final HttpServletRequest request;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final Unleash unleash;

    public LoginController( LogoutService logoutService,
                           AuthenticationService authenticationService,
                           HttpServletRequest request,
                           LoginService loginService,Unleash unleash) {
        this.authenticationService = authenticationService;
        this.request = request;
        this.loginService = loginService;
        this.logoutService = logoutService;
        this.unleash = unleash;
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
    public ResponseEntity<GeneralApiResponse<LoginResponse>> authenticate(@Valid @RequestBody LoginUserDto loginUserDto, @RequestHeader("User-Agent") String userAgent) {
        if(unleash.isEnabled("2fa_enabled")){
            logger.info("2FA is enabled");
        }
        LoginProcessParameters loginProcessParameters = new LoginProcessParameters();
        loginProcessParameters.setLoginRequest(loginUserDto);
        loginProcessParameters.setUserAgent(userAgent);
        loginProcessParameters.setRequest(request);
        return loginService.handleLogin(loginProcessParameters);
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
//        logoutService.handleLogout(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "refreshToken=; Max-Age=0; Path=/; HttpOnly; SameSite=None; Secure");
        headers.add(HttpHeaders.SET_COOKIE, "jweToken=; Max-Age=0; Path=/; HttpOnly; SameSite=None; Secure");
        headers.add(HttpHeaders.SET_COOKIE, "isAuthenticated=; Max-Age=0; Path=/; SameSite=None; Secure");
        return ResponseEntity.ok().headers(headers).body("Logged out successfully");
    }
}

package com.multicloud.auth.service.auth;

import com.multicloud.auth.dto.LoginProcessParameters;
import com.multicloud.auth.dto.LoginUserDto;
import com.multicloud.auth.dto.responses.GeneralApiResponse;
import com.multicloud.auth.dto.responses.LoginResponse;
import com.multicloud.auth.entity.LoginAttempt;
import com.multicloud.auth.entity.RefreshToken;
import com.multicloud.auth.entity.User;
import com.multicloud.auth.repository.LoginAttemptRepository;
import com.multicloud.auth.repository.UserRepository;
import com.multicloud.auth.service.AsyncEmailNotificationService;
import com.multicloud.auth.service.JweService;
import com.multicloud.auth.util.CookieUtil;
import com.multicloud.auth.util.RequestUtil;
import com.multicloud.commonlib.constants.AuthConstants;
import com.multicloud.commonlib.constants.DeviceConstants;
import com.multicloud.commonlib.exceptions.AccountLockedException;
import com.multicloud.commonlib.exceptions.AccountNotVerifiedException;
import com.multicloud.commonlib.exceptions.TooManyDeviceAttemptsException;
import com.multicloud.commonlib.exceptions.UsernameNotFoundException;
import com.multicloud.commonlib.util.common.InputSanitizer;
import com.multicloud.commonlib.util.common.LoginTimeUtil;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);
    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final AuthenticationManager authenticationManager;
    private final JweService jweService;
    private final UserRepository userRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final AsyncEmailNotificationService asyncEmailNotificationService;
    private final RefreshTokenService refreshTokenService;

    @Value("${auth.token.expiry.remember-days:30}")
    private int rememberExpiryDays;

    @Value("${auth.token.expiry.normal-days:7}")
    private int normalExpiryDays;

    @Value("${auth.failure.max-attempts:10}")
    private int maxAuthAttempts;

    @Value("${auth.failure.device-max-attempts:5}")
    private int maxDeviceAttempts;

    @Value("${auth.failure.global-max-attempts:2}")
    private int globalMaxAttempts;

    @Value("${auth.failure.lockout-window-hours:1}")
    private int lockoutWindowHours;

    @Value("${auth.failure.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    @Value("${auth.cookie.same-site:Lax}")
    private String cookieSameSite;

    public LoginService(
            AuthenticationManager authenticationManager,
            JweService jweService,
            UserRepository userRepository,
            LoginAttemptRepository loginAttemptRepository,
            AsyncEmailNotificationService asyncEmailNotificationService,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jweService = jweService;
        this.userRepository = userRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.asyncEmailNotificationService = asyncEmailNotificationService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public ResponseEntity<GeneralApiResponse<LoginResponse>> handleLogin(LoginProcessParameters loginProcessParameters) {
        validateLoginRequest(loginProcessParameters.getLoginRequest());
        loginProcessParameters.setClientIp(RequestUtil.getClientIp(loginProcessParameters.getRequest()));
        loginProcessParameters.setNow(LocalDateTime.now(ZoneOffset.UTC));
        LocalDateTime lockWindowStart = loginProcessParameters.getNow().minusHours(lockoutWindowHours);
        long failedAttemptsFromDevice = loginAttemptRepository.countFailedAttemptsByVisitorId(
                loginProcessParameters.getLoginRequest().getEmail(), loginProcessParameters.getLoginRequest().getVisitorId(), lockWindowStart);
        loginProcessParameters.setFailedAttemptsFromDevice(failedAttemptsFromDevice);
        if (failedAttemptsFromDevice >= maxDeviceAttempts) {
            log.warn("Too many failed attempts from device for visitorId: {}", loginProcessParameters.getLoginRequest().getVisitorId());
            throw new TooManyDeviceAttemptsException("Too many failed attempts from this device");
        }
        long uniqueVisitorIdFailures = loginAttemptRepository.countDistinctFailedVisitorIds(loginProcessParameters.getLoginRequest().getEmail(), lockWindowStart);
        loginProcessParameters.setUniqueVisitorIdFailures(uniqueVisitorIdFailures);
        return processAuthenticationFlow(loginProcessParameters);
    }

    private ResponseEntity<GeneralApiResponse<LoginResponse>> processAuthenticationFlow(LoginProcessParameters loginProcessParameters) {
        Optional<User> cachedUserOpt = getUserByEmail(loginProcessParameters.getLoginRequest().getEmail());
        try {
            User user = authenticateUser(loginProcessParameters, cachedUserOpt);
            log.info("User login successful - userId: {}, IP: {}", user.getId(), loginProcessParameters.getClientIp());
            recordLoginAttempt(user, loginProcessParameters.getLoginRequest().getEmail(), true, loginProcessParameters.getClientIp(), loginProcessParameters.getUserAgent(), null, loginProcessParameters.getLoginRequest().getVisitorId());
            RefreshToken refreshToken = refreshTokenService.handleRefreshToken(user, loginProcessParameters);
            updateUserLoginInfo(user, loginProcessParameters.getClientIp(), loginProcessParameters.getNow());
            boolean isSecure = RequestUtil.isRequestSecure(loginProcessParameters.getRequest());
            if (user.getFailedAttempts() >= maxDeviceAttempts && loginProcessParameters.getUniqueVisitorIdFailures() >= globalMaxAttempts) {
                List<LoginAttempt> loginAttempts = loginAttemptRepository.findRecentFailedAttemptsByEmailExcludingIpAndVisitorId(loginProcessParameters.getLoginRequest().getEmail(), loginProcessParameters.getLoginRequest().getVisitorId(), loginProcessParameters.getNow().minusHours(lockoutWindowHours));
                asyncEmailNotificationService.produceLoginFromNewDeviceNotification(loginAttempts, user.getFirstName(), loginProcessParameters.getLoginRequest().getEmail());
            }
            return buildSuccessResponse(user, refreshToken, loginProcessParameters.getLoginRequest().isRemember(), isSecure);
        } catch (Exception e) {
            recordLoginAttempt(cachedUserOpt.orElse(null), loginProcessParameters.getLoginRequest().getEmail(), false, loginProcessParameters.getClientIp(), loginProcessParameters.getUserAgent(), e.getMessage(), loginProcessParameters.getLoginRequest().getVisitorId());
            throw e;
        }
    }


    private void recordLoginAttempt(User user, String email, boolean successful, String ip, String userAgent, String failureReason, String visitorId) {
        try {
            LoginAttempt attempt = new LoginAttempt(user, email, successful, ip, userAgent, failureReason, visitorId);
            loginAttemptRepository.save(attempt);
        } catch (Exception e) {
            log.error("Failed to record login attempt", e);
        }
    }

    private void validateLoginRequest(@NotNull LoginUserDto loginRequest) {
        if (!StringUtils.hasText(loginRequest.getEmail()) || !StringUtils.hasText(loginRequest.getPassword())) {
            throw new IllegalArgumentException("Email and password must not be empty");
        }
    }

    private Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    protected User authenticateUser(LoginProcessParameters loginProcessParameters, Optional<User> cachedUserOpt) {
        if (cachedUserOpt.isEmpty()) {
            throw new UsernameNotFoundException(INVALID_CREDENTIALS);
        }
        User user = cachedUserOpt.get();
        boolean updated = verifyCredentials(user, loginProcessParameters);
        updated |= checkAccountStatus(user, loginProcessParameters.getNow());
        if (updated) {
            userRepository.save(user);
        }
        return user;
    }

    private boolean checkAccountStatus(User user, LocalDateTime now) {
        boolean updated = false;
        if (user.isLocked()) {
            if (user.getLockoutEnd() != null && user.getLockoutEnd().isAfter(now)) {
                throw new AccountLockedException("Account temporarily locked. Try again later.");
            }
            user.setLocked(false);
            user.setFailedAttempts(0);
            updated = true;
        }
        return updated;
    }

    private boolean verifyCredentials(User user, LoginProcessParameters loginProcessParameters) {
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please verify your account.");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginProcessParameters.getLoginRequest().getEmail(),
                            loginProcessParameters.getLoginRequest().getPassword()));

            // Reset failed attempts on successful login
            if (user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                return true;
            }
        } catch (AuthenticationException e) {
            handleFailedLogin(user, loginProcessParameters);
            throw e;
        }
        return false;
    }

    private void handleFailedLogin(User user, LoginProcessParameters loginProcessParameters) {
        boolean shouldLock = loginProcessParameters.getFailedAttemptsFromDevice() >= maxDeviceAttempts
                && loginProcessParameters.getUniqueVisitorIdFailures() >= globalMaxAttempts;
        int newAttempts = user.getFailedAttempts() + 1;
        boolean willLock = shouldLock || newAttempts >= maxAuthAttempts;
        userRepository.updateFailedAttemptsAndLockStatus(
                user.getId(),
                newAttempts,
                willLock,
                willLock ? loginProcessParameters.getNow().plus(getLockoutDuration()) : null
        );
        if (willLock) {
            String lockTime = LoginTimeUtil.formatLoginTime(loginProcessParameters.getNow(), loginProcessParameters.getRequest().getHeader(DeviceConstants.HEADER_TIMEZONE));
            asyncEmailNotificationService.produceAccountLockNotification(user.getEmail(), loginProcessParameters.getClientIp(), lockTime, user.getFirstName());
        }
    }

    private void updateUserLoginInfo(User user, String ip, LocalDateTime now) {
        userRepository.updateLastLogin(user.getId(), now, ip);
    }

    private ResponseEntity<GeneralApiResponse<LoginResponse>> buildSuccessResponse(User user, RefreshToken token,
                                                                                   boolean rememberMe, boolean isSecure) {
        String jwe = jweService.generateToken(user);
        Duration refreshExpiry = rememberMe ? Duration.ofDays(rememberExpiryDays) : Duration.ofDays(normalExpiryDays);

        ResponseCookie refreshCookie = CookieUtil.createCookie(
                AuthConstants.REFRESH_TOKEN_COOKIE_NAME, token.getToken(),
                refreshExpiry, true, isSecure, cookieSameSite);

        ResponseCookie jweCookie = CookieUtil.createCookie(
                AuthConstants.JWE_TOKEN_COOKIE_NAME, jwe,
                Duration.ofMillis(jweService.getExpirationTime()), true, isSecure, cookieSameSite);

        String userId = user.getId() != null ? InputSanitizer.sanitize(user.getId().toString()) : null;
        String userName = InputSanitizer.sanitize(user.getUsername());
        String lastLogin = user.getLastLogin() != null ? InputSanitizer.sanitize(user.getLastLogin().toString()) : null;
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jweCookie.toString())
                .body(GeneralApiResponse.success("Login successful", new LoginResponse(userId, userName, lastLogin)));
    }

    private Duration getLockoutDuration() {
        return Duration.ofMinutes(lockoutDurationMinutes);
    }
}
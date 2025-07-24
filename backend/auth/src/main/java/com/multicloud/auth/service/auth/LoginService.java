package com.multicloud.auth.service.auth;

import com.multicloud.auth.dto.LoginUserDto;
import com.multicloud.auth.dto.responses.GeneralApiResponse;
import com.multicloud.auth.dto.responses.LoginResponse;
import com.multicloud.auth.entity.LoginAttempt;
import com.multicloud.auth.entity.RefreshToken;
import com.multicloud.auth.entity.User;
import com.multicloud.auth.repository.LoginAttemptRepository;
import com.multicloud.auth.repository.RefreshTokenRepository;
import com.multicloud.auth.repository.UserRepository;
import com.multicloud.auth.service.AsyncEmailNotificationService;
import com.multicloud.auth.service.JweService;
import com.multicloud.auth.util.CookieUtil;
import com.multicloud.auth.util.RequestUtil;
import com.multicloud.auth.util.UserAgentParser;
import com.multicloud.commonlib.constants.AuthConstants;
import com.multicloud.commonlib.constants.DeviceConstants;
import com.multicloud.commonlib.exceptions.*;
import com.multicloud.commonlib.util.common.InputSanitizer;
import com.multicloud.commonlib.util.common.LoginTimeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.UUID;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);
    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final AuthenticationManager authenticationManager;
    private final JweService jweService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final AsyncEmailNotificationService asyncEmailNotificationService;

    @Value("${auth.token.expiry.remember-days:30}")
    private int rememberExpiryDays;

    @Value("${auth.token.expiry.normal-days:7}")
    private int normalExpiryDays;

    @Value("${auth.token.max-sessions:5}")
    private int maxSessions;

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
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            LoginAttemptRepository loginAttemptRepository,
            AsyncEmailNotificationService asyncEmailNotificationService) {
        this.authenticationManager = authenticationManager;
        this.jweService = jweService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.asyncEmailNotificationService = asyncEmailNotificationService;
    }

    @Transactional
    public ResponseEntity<GeneralApiResponse<LoginResponse>> handleLogin(LoginUserDto loginRequest, String userAgent, HttpServletRequest request) {
        try {
            validateLoginRequest(loginRequest);
            String clientIp = RequestUtil.getClientIp(request);
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime lockWindowStart = now.minusHours(lockoutWindowHours);
            long failedAttemptsFromDevice = loginAttemptRepository.countFailedAttemptsByVisitorId(
                    loginRequest.getEmail(), loginRequest.getVisitorId(), lockWindowStart);
            if (failedAttemptsFromDevice >= maxDeviceAttempts) {
                log.warn("Too many failed attempts from device for email: {}", DigestUtils.sha256Hex(InputSanitizer.sanitize(loginRequest.getEmail())));
                throw new TooManyDeviceAttemptsException("Too many failed attempts from this device");
            }
            long uniqueVisitorIdFailures = loginAttemptRepository.countDistinctFailedVisitorIds(loginRequest.getEmail(), lockWindowStart);
            return processAuthenticationFlow(loginRequest, userAgent, request, clientIp, now, failedAttemptsFromDevice, uniqueVisitorIdFailures);
        } catch (Exception e) {
            String exceptionName = e.getClass().getSimpleName();
            String emailHash = DigestUtils.sha256Hex(loginRequest.getEmail());
            log.error("{} occurred during login [email_hash={}]", exceptionName, emailHash);
            throw e;
        }
    }

    private ResponseEntity<GeneralApiResponse<LoginResponse>> processAuthenticationFlow(LoginUserDto loginRequest, String userAgent, HttpServletRequest request, String clientIp, LocalDateTime now, long failedAttemptsFromDevice, long uniqueVisitorIdFailures) {
        Optional<User> cachedUserOpt = getUserByEmail(loginRequest.getEmail());
        try {
            User user = authenticateUser(loginRequest, now, clientIp, failedAttemptsFromDevice, uniqueVisitorIdFailures, cachedUserOpt);
            log.info("User login successful - userId: {}, IP: {}", user.getId(), clientIp);
            recordLoginAttempt(user, loginRequest.getEmail(), true, clientIp, userAgent, null, loginRequest.getVisitorId());
            RefreshToken refreshToken = handleRefreshToken(user, loginRequest, userAgent, clientIp, now, request);
            updateUserLoginInfo(user, clientIp, now);
            boolean isSecure = RequestUtil.isRequestSecure(request);
            if (user.getFailedAttempts() >= maxDeviceAttempts && uniqueVisitorIdFailures >= globalMaxAttempts) {
                asyncEmailNotificationService.produceLoginFromNewDeviceNotification(user, clientIp, userAgent, now, InputSanitizer.sanitize(request.getHeader(DeviceConstants.HEADER_TIMEZONE)));
            }
            return buildSuccessResponse(user, refreshToken, loginRequest.isRemember(), isSecure);
        } catch (Exception e) {
            recordLoginAttempt(cachedUserOpt.orElse(null), loginRequest.getEmail(), false, clientIp, userAgent, e.getMessage(), loginRequest.getVisitorId());
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

    protected User authenticateUser(LoginUserDto loginRequest, LocalDateTime now, String clientIp, long failedAttemptsFromDevice, long uniqueVisitorIdFailures, Optional<User> cachedUserOpt) {
        if (cachedUserOpt.isEmpty()) {
            throw new UsernameNotFoundException(INVALID_CREDENTIALS);
        }
        User user = cachedUserOpt.get();
        boolean updated = verifyCredentials(loginRequest, user, clientIp, failedAttemptsFromDevice, now, uniqueVisitorIdFailures);
        updated |= checkAccountStatus(user, now);
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

    public boolean hasExceededSessionLimit(User user, int maxSessions, LocalDateTime now) {
        return refreshTokenRepository.countActiveTokensByUser(user, now) >= maxSessions;
    }

    private boolean verifyCredentials(LoginUserDto loginRequest, User user, String clientIp,
                                      long failedAttemptsFromDevice, LocalDateTime now, long uniqueVisitorIdFailures) {
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please verify your account.");
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            // Reset failed attempts on successful login
            if (user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                return true;
            }
        } catch (AuthenticationException e) {
            handleFailedLogin(user, failedAttemptsFromDevice, clientIp, now, uniqueVisitorIdFailures);
            throw e;
        }
        return false;
    }

    private void handleFailedLogin(User user, long deviceAttempts, String ip,
                                   LocalDateTime now, long uniqueVisitorIdFailures) {
        boolean shouldLock = deviceAttempts >= maxDeviceAttempts
                && uniqueVisitorIdFailures >= globalMaxAttempts;
        int newAttempts = user.getFailedAttempts() + 1;
        boolean willLock = shouldLock || newAttempts >= maxAuthAttempts;
        userRepository.updateFailedAttemptsAndLockStatus(
                user.getId(),
                newAttempts,
                willLock,
                willLock ? now.plus(getLockoutDuration()) : null
        );
        if (willLock) {
            asyncEmailNotificationService.produceAccountLockNotification(user.getEmail(), ip);
        }
    }

    private RefreshToken handleRefreshToken(User user, LoginUserDto loginRequest,
                                            String userAgent, String clientIp, LocalDateTime now, HttpServletRequest request) {

        Optional<RefreshToken> existingOpt = refreshTokenRepository.lockByUserAndVisitorId(user, loginRequest.getVisitorId());
        if (existingOpt.isPresent()) {
            RefreshToken existing = existingOpt.get();
            if (!shouldRotateToken(existing, now)) {
                log.info("Reusing existing token ID {} for userId {}", existing.getId(), user.getId());
                return existing;
            }
            existing.revoke(clientIp);
            refreshTokenRepository.save(existing);
        }
        if (hasExceededSessionLimit(user, maxSessions, now)) {
            throw new TooManySessionsException("Maximum active sessions reached. Please logout from another device.");
        }
        String tokenValue = UUID.randomUUID().toString();
        String timezoneId = request.getHeader(DeviceConstants.HEADER_TIMEZONE);
        LocalDateTime newExpiry = now.plusDays(loginRequest.isRemember() ? rememberExpiryDays : normalExpiryDays);
        String deviceInfo = UserAgentParser.buildDeviceInfo(userAgent, request);
        String loginTime = LoginTimeUtil.formatLoginTime(now, timezoneId);
        return createNewRefreshToken(user, tokenValue, newExpiry, deviceInfo, clientIp, loginRequest.getVisitorId(), loginTime);
    }

    private boolean shouldRotateToken(RefreshToken existing, LocalDateTime now) {
        if (existing.isExpired() || existing.isRevoked()) return true;

        // Rotate if it will expire soon
        Duration remaining = Duration.between(now, existing.getExpiryDate());
        Duration total = Duration.between(existing.getCreatedAt(), existing.getExpiryDate());
        return remaining.toMinutes() < (total.toMinutes() * 0.1); // less than 10% lifetime left
    }

    private RefreshToken createNewRefreshToken(User user, String tokenValue, LocalDateTime expiry,
                                               String deviceInfo, String ip, String visitorId, String loginTime) {
        // Check separately for visitorId and IP
        boolean isNewDevice = !refreshTokenRepository.existsByUserAndVisitorId(user, visitorId)
                && !refreshTokenRepository.existsByUserAndIpAddress(user, ip);

        RefreshToken token = new RefreshToken(user, tokenValue, expiry, deviceInfo, ip, visitorId);
        RefreshToken saved = refreshTokenRepository.save(token);

        if (isNewDevice) {
            log.info("New device login detected - userId: {}, IP: {}", user.getId(), ip);
            asyncEmailNotificationService.produceLoginAlertNotification(user, ip, deviceInfo, loginTime);
        }

        return saved;
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

    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveSessions(User user) {
        return refreshTokenRepository.findActiveTokensByUser(user, LocalDateTime.now());
    }

    @Transactional
    public void revokeSession(User user, Long tokenId, String revokedByIp) {
        refreshTokenRepository.revokeTokenForUser(tokenId, user, LocalDateTime.now(), revokedByIp);
    }

    @Transactional
    public void revokeAllOtherSessions(User user, Long currentTokenId, String revokedByIp) {
        refreshTokenRepository.revokeAllForUserExcept(user, currentTokenId, LocalDateTime.now(), revokedByIp);
    }

    private Duration getLockoutDuration() {
        return Duration.ofMinutes(lockoutDurationMinutes);
    }
}
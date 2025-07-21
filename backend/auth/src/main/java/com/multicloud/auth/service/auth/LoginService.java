package com.multicloud.auth.service.auth;

import com.multicloud.auth.dto.LoginUserDto;
import com.multicloud.auth.dto.responses.LoginResponse;
import com.multicloud.auth.entity.*;
import com.multicloud.auth.repository.*;
import com.multicloud.auth.service.AsyncEmailNotificationService;
import com.multicloud.auth.service.JweService;
import com.multicloud.auth.util.CookieUtil;
import com.multicloud.auth.util.UserAgentParser;
import com.multicloud.commonlib.constants.AuthConstants;
import com.multicloud.commonlib.exceptions.*;
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
import java.util.UUID;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(30);
    private static final String UNKNOWN_DEVICE = "unknown:unknown:unknown";
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
    public ResponseEntity<LoginResponse> handleLogin(LoginUserDto loginRequest, String userAgent, HttpServletRequest request) {
        try {
            validateLoginRequest(loginRequest);
            String clientIp = getClientIp(request);
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            try {
                User user = authenticateUser(loginRequest);
                if (user != null) {
                    log.info("User login successful - userId: {}, IP: {}", user.getId(), clientIp);
                    recordLoginAttempt(user, loginRequest.getEmail(), true, clientIp, userAgent, null);

                    RefreshToken refreshToken = handleRefreshToken(user, loginRequest, userAgent, clientIp, now, request);
                    updateUserLoginInfo(user, clientIp, now);

                    boolean isSecure = isRequestSecure(request);
                    return buildSuccessResponse(user, refreshToken, loginRequest.isRemember(), isSecure);
                }
                log.warn("User not found for email: {}", loginRequest.getEmail());
                return ResponseEntity.status(401).body(new LoginResponse(INVALID_CREDENTIALS));
            } catch (AccountNotVerifiedException e) {
                log.warn("Account not verified: {}", e.getMessage());
                recordLoginAttempt(null, loginRequest.getEmail(), false, clientIp, userAgent, e.getMessage());
                return ResponseEntity.status(403).body(new LoginResponse(e.getMessage()));
            } catch (AccountLockedException e) {
                log.warn("Account locked: {}", e.getMessage());
                recordLoginAttempt(null, loginRequest.getEmail(), false, clientIp, userAgent, e.getMessage());
                return ResponseEntity.status(401).body(new LoginResponse(INVALID_CREDENTIALS));
            } catch (AuthenticationException e) {
                User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
                log.warn("Authentication failed for user [email_hash={}]", DigestUtils.sha256Hex(loginRequest.getEmail()));
                recordLoginAttempt(user, loginRequest.getEmail(), false, clientIp, userAgent, INVALID_CREDENTIALS);
                return ResponseEntity.status(401).body(new LoginResponse(INVALID_CREDENTIALS));
            } catch (UsernameNotFoundException e) {
                log.warn("Authentication failed for user [email_hash={}] as user doesn't exist", DigestUtils.sha256Hex(loginRequest.getEmail()));
                return ResponseEntity.status(401).body(new LoginResponse(INVALID_CREDENTIALS));
            }
        } catch (Exception e) {
            log.error("Unexpected login error for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(500).body(new LoginResponse("Login error"));
        }
    }



    private void recordLoginAttempt(User user, String email, boolean successful, String ip, String userAgent, String failureReason) {
        try {
            LoginAttempt attempt = new LoginAttempt(user, email, successful, ip, userAgent, failureReason);
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

    protected User authenticateUser(LoginUserDto loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        checkAccountStatus(user);
        verifyCredentials(loginRequest, user);
        return user;
    }

    private void checkAccountStatus(User user) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please verify your account.");
        }
        if (user.isLocked()) {
            if (user.getLockoutEnd() != null && user.getLockoutEnd().isAfter(now)) {
                throw new AccountLockedException("Account temporarily locked. Try again later.");
            }
            user.setLocked(false);
            user.setFailedAttempts(0);
            userRepository.save(user);
        }
    }

    public boolean hasExceededSessionLimit(User user, int maxSessions) {
        return refreshTokenRepository.countActiveTokensByUser(user, LocalDateTime.now()) >= maxSessions;
    }

    private void verifyCredentials(LoginUserDto loginRequest, User user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            // Reset failed attempts on successful login
            if (user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                userRepository.save(user);
            }
        } catch (AuthenticationException e) {
            handleFailedLogin(user);
            throw e;
        }
    }

    private void handleFailedLogin(User user) {
        userRepository.incrementFailedAttemptsAndLockIfNeeded(
                user.getId(),
                MAX_LOGIN_ATTEMPTS,
                LocalDateTime.now(ZoneOffset.UTC).plus(LOCKOUT_DURATION)
        );
    }


    private RefreshToken handleRefreshToken(User user, LoginUserDto loginRequest,
                                            String userAgent, String clientIp, LocalDateTime now, HttpServletRequest request) {
        if (hasExceededSessionLimit(user, maxSessions)) {
            throw new TooManySessionsException("Maximum active sessions reached. Please logout from another device.");
        }
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiry = now.plusDays(loginRequest.isRemember() ? rememberExpiryDays : normalExpiryDays);
        String deviceInfo = buildDeviceInfo(userAgent, request);

        return refreshTokenRepository.lockByUserAndVisitorId(user, loginRequest.getVisitorId())
                .map(existing -> {
                    if (existing.isExpired() || existing.isRevoked()) {
                        // Revoke the old token before creating a new one
                        existing.revoke(clientIp);
                        refreshTokenRepository.save(existing);
                        return createNewRefreshToken(user, tokenValue, expiry, deviceInfo, clientIp, loginRequest.getVisitorId(), now);
                    }
                    return updateExistingToken(existing, tokenValue, expiry, clientIp);
                })
                .orElseGet(() -> createNewRefreshToken(user, tokenValue, expiry, deviceInfo, clientIp, loginRequest.getVisitorId(), now));
    }

    private RefreshToken updateExistingToken(RefreshToken token, String newValue,
                                             LocalDateTime expiry, String ip) {
        log.info("Reusing existing token ID {} for userId {}, rotating value", token.getId(), token.getUser().getId());
        token.updateToken(newValue, expiry, ip);
        return refreshTokenRepository.save(token);
    }

    private RefreshToken createNewRefreshToken(User user, String tokenValue, LocalDateTime expiry,
                                               String deviceInfo, String ip, String visitorId , LocalDateTime now) {
        // Check separately for visitorId and IP
        boolean isNewDevice = !refreshTokenRepository.existsByUserAndVisitorId(user, visitorId)
                && !refreshTokenRepository.existsByUserAndIpAddress(user, ip);

        RefreshToken token = new RefreshToken(user, tokenValue, expiry, deviceInfo, ip, visitorId);
        RefreshToken saved = refreshTokenRepository.save(token);

        if (isNewDevice) {
            log.info("New device login detected - userId: {}, IP: {}", user.getId(), ip);
            asyncEmailNotificationService.produceLoginAlertNotification(user, ip, deviceInfo,now);
        }

        return saved;
    }

    private void updateUserLoginInfo(User user, String ip, LocalDateTime now) {
        userRepository.updateLastLogin(user.getId(), now, ip);
    }

    private ResponseEntity<LoginResponse> buildSuccessResponse(User user, RefreshToken token,
                                                               boolean rememberMe, boolean isSecure) {
        String jwe = jweService.generateToken(user);
        Duration refreshExpiry = rememberMe ? Duration.ofDays(rememberExpiryDays) : Duration.ofDays(normalExpiryDays);

        ResponseCookie refreshCookie = CookieUtil.createCookie(
                AuthConstants.REFRESH_TOKEN_COOKIE_NAME, token.getToken(),
                refreshExpiry, true, isSecure, "Lax");

        ResponseCookie jweCookie = CookieUtil.createCookie(
                AuthConstants.JWE_TOKEN_COOKIE_NAME, jwe,
                Duration.ofMillis(jweService.getExpirationTime()), true, isSecure, "Lax");

        return ResponseEntity.ok()
                .headers(headers -> {
                    headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                    headers.add(HttpHeaders.SET_COOKIE, jweCookie.toString());
                })
                .body(new LoginResponse("Login successful"));
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

    private String buildDeviceInfo(String userAgent, HttpServletRequest request) {
        try {
            String[] parts = UserAgentParser.parseUserAgent(userAgent);
            if (parts.length < 3) return UNKNOWN_DEVICE;
            String screenResolution = request.getHeader("X-Screen-Resolution");
            String timezone = request.getHeader("X-Timezone-Offset");
            log.debug("Screen resolution: {}, Timezone: {}", screenResolution, timezone);
            return String.format("%s:%s:%s", parts[2], parts[1], parts[0]);
        } catch (Exception e) {
            log.warn("Failed to parse user agent: {}", userAgent, e);
            return UNKNOWN_DEVICE;
        }
    }

    private boolean isRequestSecure(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedSsl = request.getHeader("X-Forwarded-Ssl");
        String frontEndHttps = request.getHeader("Front-End-Https");

        return request.isSecure()
                || "https".equalsIgnoreCase(forwardedProto)
                || "on".equalsIgnoreCase(forwardedSsl)
                || "on".equalsIgnoreCase(frontEndHttps);
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIpv4 = request.getHeader("X-User-IP");
        String clientIpv6 = request.getHeader("X-User-IP-V6");
        String clientIp = "UNKNOWN";
        if (!AuthConstants.NOT_APPLICABLE.equals(clientIpv4)) {
            clientIp = clientIpv4;  // Prefer IPv4 if it's available and not "Unknown"
        } else if (!AuthConstants.NOT_APPLICABLE.equals(clientIpv6)) {
            clientIp = clientIpv6;  // Fallback to IPv6 if IPv4 is "Unknown"
        }
        return clientIp;
    }
}
package com.multicloud.auth.service.auth;

import com.multicloud.auth.dto.LoginProcessParameters;
import com.multicloud.auth.entity.RefreshToken;
import com.multicloud.auth.entity.User;
import com.multicloud.auth.repository.RefreshTokenRepository;
import com.multicloud.auth.service.AsyncEmailNotificationService;
import com.multicloud.auth.util.UserAgentParser;
import com.multicloud.commonlib.constants.DeviceConstants;
import com.multicloud.commonlib.exceptions.TooManySessionsException;
import com.multicloud.commonlib.util.common.LoginTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final AsyncEmailNotificationService asyncEmailNotificationService;
    private final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    @Value("${auth.token.expiry.remember-days:30}")
    private int rememberExpiryDays;

    @Value("${auth.token.expiry.normal-days:7}")
    private int normalExpiryDays;

    @Value("${auth.token.max-sessions:5}")
    private int maxSessions;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               AsyncEmailNotificationService asyncEmailNotificationService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.asyncEmailNotificationService = asyncEmailNotificationService;
    }

    public RefreshToken handleRefreshToken(User user, LoginProcessParameters loginProcessParameters) {
        Optional<RefreshToken> existingOpt = refreshTokenRepository.lockByUserAndVisitorId(user, loginProcessParameters.getLoginRequest().getVisitorId());
        if (existingOpt.isPresent()) {
            RefreshToken existing = existingOpt.get();
            if (!shouldRotateToken(existing, loginProcessParameters.getNow())) {
                log.info("Reusing existing token ID {} for userId {}", existing.getId(), user.getId());
                return existing;
            }
            existing.revoke(loginProcessParameters.getClientIp());
            refreshTokenRepository.save(existing);
        }
        if (hasExceededSessionLimit(user, maxSessions, loginProcessParameters.getNow())) {
            throw new TooManySessionsException("Maximum active sessions reached. Please logout from another device.");
        }
        String tokenValue = UUID.randomUUID().toString();
        String timezoneId = loginProcessParameters.getRequest().getHeader(DeviceConstants.HEADER_TIMEZONE);
        LocalDateTime newExpiry = loginProcessParameters.getNow().plusDays(loginProcessParameters.getLoginRequest().isRemember() ? rememberExpiryDays : normalExpiryDays);
        String deviceInfo = UserAgentParser.buildDeviceInfo(loginProcessParameters.getUserAgent(), loginProcessParameters.getRequest());
        String loginTime = LoginTimeUtil.formatLoginTime(loginProcessParameters.getNow(), timezoneId);
        return createNewRefreshToken(user, tokenValue, newExpiry, deviceInfo, loginProcessParameters.getClientIp(), loginProcessParameters.getLoginRequest().getVisitorId(), loginTime);
    }

    private boolean shouldRotateToken(RefreshToken existing, LocalDateTime now) {
        if (existing.isExpired() || existing.isRevoked()) return true;

        // Rotate if it will expire soon
        Duration remaining = Duration.between(now, existing.getExpiryDate());
        Duration total = Duration.between(existing.getCreatedAt(), existing.getExpiryDate());
        return remaining.toMinutes() < (total.toMinutes() * 0.1); // less than 10% lifetime left
    }

    public boolean hasExceededSessionLimit(User user, int maxSessions, LocalDateTime now) {
        return refreshTokenRepository.countActiveTokensByUser(user, now) >= maxSessions;
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
}

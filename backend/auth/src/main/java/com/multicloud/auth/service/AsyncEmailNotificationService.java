package com.multicloud.auth.service;

import com.multicloud.auth.component.EmailNotificationProducer;
import com.multicloud.auth.entity.LoginAttempt;
import com.multicloud.auth.entity.User;
import com.multicloud.auth.util.UserAgentParser;
import com.multicloud.commonlib.email.*;
import com.multicloud.commonlib.email.dto.SimpleLoginAttemptDTO;
import com.multicloud.commonlib.exceptions.EmailNotificationPublishException;
import com.multicloud.commonlib.util.common.MapUrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsyncEmailNotificationService {

    private final IpGeolocationService ipGeolocationService;
    private final String googleMapsApiKey;
    private final EmailNotificationProducer emailNotificationProducer;
    private static final Logger logger = LoggerFactory.getLogger(AsyncEmailNotificationService.class);
    @Value("${email.desktop.image.url}")
    private String desktopImagePath;
    @Value("${email.mobile.image.url}")
    private String mobileImagePath;
    @Value("${frontend.base-url}")
    private String frontendBaseUrl;
    @Value("${email.logo.url}")
    private String logoUrl;

    public AsyncEmailNotificationService(IpGeolocationService ipGeolocationService,
                                         @Value("${google.maps.api.key}") String googleMapsApiKey, EmailNotificationProducer emailNotificationProducer) {
        this.ipGeolocationService = ipGeolocationService;
        this.googleMapsApiKey = googleMapsApiKey;
        this.emailNotificationProducer = emailNotificationProducer;
    }

    @Async("taskExecutor")
    public void producePasswordResetNotification(User user) {
        PasswordResetEmailRequest request = new PasswordResetEmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("Password Reset Request for C-Cloud Account");
        request.setFirstName(user.getFirstName());
        request.setResetLink(frontendBaseUrl + "/reset-password?token=" + user.getPasswordResetToken());
        request.setLogoUrl(logoUrl);
        sendEmailNotification(request, "password-reset-email");
    }

    @Async("taskExecutor")
    public void produceLoginAlertNotification(User user, String clientIp, String userAgent, String loginTime) {
        String testIp = "27.5.140.237";
        String[] locationDetails = ipGeolocationService.getGeolocation(testIp);
        String[] deviceDetails = UserAgentParser.parseUserAgent(userAgent);
        LoginAlertEmailRequest request = new LoginAlertEmailRequest();
        request.setTo(user.getEmail());
        request.setSubject(String.format("New Login on C-Cloud from %s on %s", deviceDetails[0], deviceDetails[1]));
        request.setLogoUrl(logoUrl);
        request.setUsername(user.getUsername());
        request.setOs(deviceDetails[1]);
        request.setBrowser(deviceDetails[0]);
        request.setLocation(String.format("%s, %s", locationDetails[0], locationDetails[1]));
        request.setClientIp(clientIp);
        request.setCountry(locationDetails[2]);
        request.setFormattedLoginTime(loginTime);
        request.setMapUrl(MapUrlUtil.generateMapUrl(locationDetails[3], googleMapsApiKey));
        request.setChangePasswordUrl(frontendBaseUrl + "/change-password?email=" + user.getEmail());
        request.setDeviceImagePath(deviceDetails[2].equalsIgnoreCase("Mobile") ? mobileImagePath : desktopImagePath);
        sendEmailNotification(request, "login-alert-email");
    }

    @Async("taskExecutor")
    public void produceVerificationNotification(String to, String subject, User user) {
        VerificationEmailRequest request = new VerificationEmailRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setFirstName(user.getFirstName());
        request.setVerificationCode(user.getVerificationCode());
        request.setLogoUrl(logoUrl);
        sendEmailNotification(request, "verification-email");
    }

    @Async
    public void produceAccountLockNotification(String email, String clientIp, String lockTime, String firstname) {
        String testIp = "27.5.140.237";
        AccountLockedEmailRequest request = new AccountLockedEmailRequest();
        String[] locationDetails = ipGeolocationService.getGeolocation(testIp);
        String city = locationDetails[0];
        request.setTo(email);
        request.setSubject("Account Locked Due to Suspicious Activity");
        request.setLogoUrl(logoUrl);
        request.setFirstName(firstname);
        request.setClientIp(clientIp);
        request.setLocation(city);
        request.setLockTime(lockTime);
        request.setUnlockSupportLink(frontendBaseUrl + "/support/unlock-account?email=" + email);
        logger.info("Account lock notification for email: {} from IP: {}", email, clientIp);
        sendEmailNotification(request, "account-lock-alert-email");
    }

    @Async
    public void produceLoginFromNewDeviceNotification(List<LoginAttempt> loginAttempts, String firstName, String email) {
        List<SimpleLoginAttemptDTO> dtoAttempts = loginAttempts.stream()
                .map(this::convertToSimpleLoginAttemptDTO)
                .toList();
        SuspiciousAlertEmailRequest request = new SuspiciousAlertEmailRequest();
        request.setFirstName(firstName);
        request.setAttempts(dtoAttempts);
        request.setChangePasswordUrl(frontendBaseUrl + "/change-password?email=" + email);
        request.setLogoUrl(logoUrl);
        request.setSubject("Suspicious Login Attempts Detected");
        request.setTo(email);
        sendEmailNotification(request, "suspicious-login-alert-email");
    }

    private SimpleLoginAttemptDTO convertToSimpleLoginAttemptDTO(LoginAttempt attempt) {
        return new SimpleLoginAttemptDTO(
                attempt.getEmail(),
                attempt.getIpAddress(),
                attempt.getUserAgent(),
                attempt.getAttemptTime()
        );
    }

    private void sendEmailNotification(EmailRequest request, String templateName) {
        EmailNotification notification = new EmailNotification();
        notification.setEmailRequest(request);
        notification.setTemplateName(templateName);
        try {
            emailNotificationProducer.sendEmailNotification(notification);
            logger.info("Email notification sent successfully: {}", request.getTo());
        } catch (Exception e) {
            throw new EmailNotificationPublishException("Failed to send email notification", e);
        }
    }
}

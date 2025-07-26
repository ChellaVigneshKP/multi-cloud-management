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
        try {
            PasswordResetEmailRequest request = new PasswordResetEmailRequest();
            request.setTo(user.getEmail());
            request.setSubject("Password Reset Request for C-Cloud Account");
            request.setFirstName(user.getFirstName());
            request.setResetLink(frontendBaseUrl + "/reset-password?token=" + user.getPasswordResetToken());
            request.setLogoUrl(logoUrl);
            EmailNotification notification = new EmailNotification();
            notification.setEmailRequest(request);
            notification.setTemplateName("password-reset-email");
            emailNotificationProducer.sendEmailNotification(notification);
            logger.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            throw new EmailNotificationPublishException("Failed to send password reset email to " + user.getEmail(), e);
        }
    }

    @Async("taskExecutor")
    public void produceLoginAlertNotification(User user, String clientIp, String userAgent, String loginTime) {
        String testIp = "27.5.140.237";
        String[] locationDetails = ipGeolocationService.getGeolocation(testIp);
        String city = locationDetails[0];
        String region = locationDetails[1];
        String country = locationDetails[2];
        String loc = locationDetails[3]; // Latitude, Longitude
        String mapUrl = MapUrlUtil.generateMapUrl(loc, googleMapsApiKey);
        String[] od = UserAgentParser.parseUserAgent(userAgent);
        String browser = od[0];
        String os = od[1];
        String device = od[2];
        LoginAlertEmailRequest request = new LoginAlertEmailRequest();
        request.setTo(user.getEmail());
        request.setSubject("New Login on C-Cloud from " + browser + " on " + os);
        request.setLogoUrl(logoUrl);
        request.setUsername(user.getUsername());
        request.setOs(os);
        request.setBrowser(browser);
        request.setLocation(city + ", " + region);
        request.setClientIp(clientIp);
        request.setCountry(country);
        request.setFormattedLoginTime(loginTime);
        request.setMapUrl(mapUrl);
        request.setChangePasswordUrl(frontendBaseUrl + "/change-password?email=" + user.getEmail());
        request.setDeviceImagePath(device.equalsIgnoreCase("Mobile") ? mobileImagePath : desktopImagePath);
        EmailNotification notification = new EmailNotification();
        notification.setEmailRequest(request);
        notification.setTemplateName("login-alert-email");
        emailNotificationProducer.sendEmailNotification(notification);
    }

    @Async("taskExecutor")
    public void produceVerificationNotification(String to, String subject, User user) {
        VerificationEmailRequest request = new VerificationEmailRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setFirstName(user.getFirstName());
        request.setVerificationCode(user.getVerificationCode());
        request.setLogoUrl(logoUrl);
        EmailNotification notification = new EmailNotification();
        notification.setEmailRequest(request);
        notification.setTemplateName("verification-email");
        emailNotificationProducer.sendEmailNotification(notification);
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
    }

    @Async
    public void produceLoginFromNewDeviceNotification(List<LoginAttempt> loginAttempts, String firstName, String email) {
        List<SimpleLoginAttemptDTO> dtoAttempts = loginAttempts.stream()
                .map(a -> new SimpleLoginAttemptDTO(
                        a.getEmail(),
                        a.getIpAddress(),
                        a.getUserAgent(),
                        a.getAttemptTime()
                ))
                .toList();
        SuspiciousAlertEmailRequest request = new SuspiciousAlertEmailRequest();
        request.setFirstName(firstName);
        request.setAttempts(dtoAttempts);
        request.setChangePasswordUrl(frontendBaseUrl + "/change-password?email=" + email);
        request.setLogoUrl(logoUrl);
        request.setSubject("Suspicious Login Attempts Detected");
        request.setTo(email);
        EmailNotification notification = new EmailNotification();
        notification.setEmailRequest(request);
        notification.setTemplateName("suspicious-login-alert-email");
        emailNotificationProducer.sendEmailNotification(notification);
    }

}

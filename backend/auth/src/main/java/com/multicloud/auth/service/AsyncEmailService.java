package com.multicloud.auth.service;

import com.multicloud.auth.dto.LoginAlertDto;
import com.multicloud.auth.exception.EmailSendingException;
import com.multicloud.auth.model.User;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AsyncEmailService {

    private final EmailService emailService;
    private final IpGeolocationService ipGeolocationService;
    private final String googleMapsApiKey;
    private static final Logger logger = LoggerFactory.getLogger(AsyncEmailService.class);
    @Value("${email.desktop.image.url}")
    private String desktopImagePath;
    @Value("${email.mobile.image.url}")
    private String mobileImagePath;
    public AsyncEmailService(EmailService emailService, IpGeolocationService ipGeolocationService,
                             @Value("${google.maps.api.key}") String googleMapsApiKey) {
        this.emailService = emailService;
        this.ipGeolocationService = ipGeolocationService;
        this.googleMapsApiKey = googleMapsApiKey;
    }

    @Async("taskExecutor")
    public void sendPasswordResetEmailAsync(User user) {
        try {
            emailService.sendPasswordResetEmail(user);  // This can throw MessagingException
            logger.info("Password reset email sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            throw new EmailSendingException("Failed to send password reset email to " + user.getEmail(), e);
        }
    }

    @Async("taskExecutor")
    public void sendIpChangeAlertEmailAsync(User user, String clientIp, String userAgent) {
        String testIp = "27.4.253.24";
        String[] locationDetails = ipGeolocationService.getGeolocation(testIp);
        String city = locationDetails[0];
        String region = locationDetails[1];
        String country = locationDetails[2];
        String loc = locationDetails[3]; // Latitude, Longitude
        String mapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=" + loc
                + "&zoom=13&size=600x300&maptype=roadmap"
                + "&markers=color:red%7C" + loc
                + "&key=" + googleMapsApiKey;
        ZonedDateTime loginTime = ZonedDateTime.of(user.getLastLogin(), ZoneId.systemDefault());
        String formattedLoginTime = loginTime.format(DateTimeFormatter.ofPattern("MMMM dd 'at' hh:mm a z"));
        String[] od = UserAgentParser.parseUserAgent(userAgent);
        String browser = od[0];
        String os = od[1];
        String device = od[2];
        String deviceImagePath = device.equalsIgnoreCase("Mobile") ? mobileImagePath : desktopImagePath;
        LoginAlertDto loginAlertDto = LoginAlertDto.builder()
                .user(user)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .city(city)
                .region(region)
                .country(country)
                .lastLogin(loginTime)
                .mapUrl(mapUrl)
                .formattedLoginTime(formattedLoginTime)
                .deviceImagePath(deviceImagePath)
                .os(os)
                .browser(browser)
                .build();
        logger.info("User {} logged in from new IP Address: {} from City {}, Region {}, Country {}, Location {} and Device {}, OS {}, Browser {} at {}",
                loginAlertDto.getUser().getUsername(),
                loginAlertDto.getClientIp(),
                city,
                region,
                country,
                loc,
                device,
                os,
                browser,
                formattedLoginTime
        );
        String subject = "New Login on MultiCloud from "+browser+" on "+os;
        try {
            emailService.sendIpChangeAlertEmail(loginAlertDto.getUser().getEmail(), subject, loginAlertDto);  // Pass loginAlertDto instead of htmlMessage
            logger.info("Login alert email sent successfully to '{}'", loginAlertDto.getUser().getEmail());
        } catch (MessagingException e) {
            logger.error("Failed to send new IP alert email to user: {}", loginAlertDto.getUser().getEmail(), e);
        }
    }
}

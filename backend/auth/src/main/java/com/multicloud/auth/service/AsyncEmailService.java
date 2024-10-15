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
    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

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
    public void sendIpChangeAlertEmailAsync(LoginAlertDto loginAlertDto) {
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
        ZonedDateTime loginTime = ZonedDateTime.of(loginAlertDto.getUser().getLastLogin(), ZoneId.systemDefault());
        String formattedLoginTime = loginTime.format(DateTimeFormatter.ofPattern("MMMM dd 'at' hh:mm a z"));
        String deviceInfo = loginAlertDto.getUserAgent();
        String[] od = UserAgentParser.parseUserAgent(deviceInfo);
        String os = od[1];
        String browser = od[0];
        String device = od[2];
        String desktopImagePath = "https://i.postimg.cc/3xvNxR9K/desktop.png";
        String mobileImagePath = "https://i.postimg.cc/SKbs2htc/mobile.png";
        String deviceImagePath;
        if (device.equalsIgnoreCase("Mobile")) {
            deviceImagePath = mobileImagePath;
        } else {
            deviceImagePath = desktopImagePath;
        }
        String logo="https://i.postimg.cc/tgdgFMLw/logo.png";
        logger.info("User {} logged in from new Ip Address: {} from City {}, Region {}, Country {}, Location {} and Device {}, OS {}, Browser {} at {}",loginAlertDto.getUser().getUsername(),loginAlertDto.getClientIp(),city,region,country,loc,device,os,browser,formattedLoginTime);
        String subject = "New Login on MultiCloud from "+browser+" on "+os;
        String htmlMessage = "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>New Login Alert</title>"
                + "</head>"
                + "<body style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #eef2f5; margin: 0; padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.1);\">"

                // Header
                + "<div style=\"padding: 30px 20px; text-align: center; background: linear-gradient(135deg, #B45C39, #E2725B);\">"
                + "  <img src='" + logo + "' alt='YourApp Logo' style=\"height: 50px;\">"
                + "</div>"

                // Title
                + "<div style=\"padding: 20px; text-align: center; color: #333333; font-size: 24px; font-weight: 600;\">"
                + "  We've noticed a new login, " + loginAlertDto.getUser().getUsername()
                + "</div>"

                // Subtitle
                + "<div style=\"padding: 0 20px 20px 20px; text-align: center; color: #555555; font-size: 16px;\">"
                + "  We've noticed a login from a device that you don't usually use."
                + "</div>"

                // Device Section
                + "<div style=\"text-align: center; padding: 30px 20px;\">"
                + "  <img src='" + deviceImagePath + "' alt='Device Icon' style=\"height: 60px;\">"
                + "</div>"

                // Device Info
                + "<div style=\"padding: 20px; text-align: center; color: #333333; font-size: 16px; font-weight: 500;\">"
                + "  " + os + " · " + browser + " · " + city + ", " + region + " · " + country
                + "</div>"

                // Login Time
                + "<div style=\"padding: 10px 20px; text-align: center; color: #777777; font-size: 14px;\">"
                + "  " + formattedLoginTime
                + "</div>"

                // Divider
                + "<div style=\"padding: 10px 0; text-align: center;\">"
                + "  <hr style=\"border: none; border-top: 1px solid #dddddd; width: 80%;\">"
                + "</div>"

                // Body Content
                + "<div style=\"padding: 20px 40px; color: #333333; font-size: 16px; line-height: 1.6; text-align: center;\">"
                + "  <p>We detected a login from <strong>" + os + " device</strong> on <strong>" + browser + "</strong> with the IP address: <strong>" + loginAlertDto.getClientIp() + "</strong>.</p>"
                + "  <p>If this was you, you can safely ignore this message. If not, please <a href=\"" + frontendBaseUrl + "/change-password?email=" + loginAlertDto.getUser().getEmail() + "\" style=\"color: #B45C39; text-decoration: underline;\">change your password</a> immediately.</p>"
                + "</div>"

                // Map Section
                + "<div style=\"text-align: center; padding: 20px 0;\">"
                + "  <img src=\"" + mapUrl + "\" alt=\"Login Location Map\" style=\"border-radius: 8px; max-width: 80%; height: auto; box-shadow: 0 4px 8px rgba(0,0,0,0.1);\">"
                + "</div>"

                // Action Button
                + "<div style=\"text-align: center; padding: 20px;\">"
                + "  <a href=\"" + frontendBaseUrl + "/change-password?email=" + loginAlertDto.getUser().getEmail() + "\" style=\"background-color: #B45C39; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-size: 16px; transition: background-color 0.3s ease;\">Change Your Password</a>"
                + "</div>"

                // Footer
                + "<div style=\"background-color: #f2f4f7; padding: 20px 40px; color: #777777; font-size: 14px; text-align: center;\">"
                + "  <p>If you have any questions, feel free to <a href=\"mailto:info@chellavignesh.com\" style=\"color: #B45C39; text-decoration: underline;\">contact us</a>.</p>"
                + "  <p>&copy; 2024 chellavignesh.com. All rights reserved.</p>"
                + "</div>"

                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(loginAlertDto.getUser().getEmail(), subject, htmlMessage);  // Reusing the existing sendVerificationEmail method
        } catch (MessagingException e) {
            logger.error("Failed to send new IP alert email to user: {}", loginAlertDto.getUser().getEmail(), e);
        }
    }
}

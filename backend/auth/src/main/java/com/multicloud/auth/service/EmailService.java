package com.multicloud.auth.service;

import com.multicloud.auth.dto.LoginAlertDto;
import com.multicloud.auth.model.User;
import jakarta.mail.MessagingException;  // Exception for messaging errors
import jakarta.mail.internet.MimeMessage;  // Class for MIME messages
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;  // Interface for sending emails
import org.springframework.mail.javamail.MimeMessageHelper;  // Helper class for creating MIME messages
import org.springframework.stereotype.Service;  // Indicates that this class is a service component
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

@Service  // Marks this class as a Spring service
public class EmailService {
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${email.logo.url}")
    private String logoUrl;

    public EmailService(JavaMailSender emailSender, SpringTemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    private void sendEmail(String to, String subject, String templateName, Context context) throws MessagingException {
        String htmlContent = templateEngine.process(templateName, context);
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);  // true indicates multipart message

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);  // Set to true for HTML

        emailSender.send(message);
    }
    public void sendVerificationEmail(String to, String subject, User user) throws MessagingException {
        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("verificationCode", user.getVerificationCode());  // Removed the prefix
        sendEmail(to, subject, "verification-email", context);
    }

    public void sendPasswordResetEmail(User user) throws MessagingException {
        String to = user.getEmail();
        String subject = "Password Reset Request";
        String resetLink = frontendBaseUrl+"/reset-password?token=" + user.getPasswordResetToken(); // Use HTTPS and configurable URL
        Context context = new Context();
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("resetLink", resetLink);
        sendEmail(to, subject, "password-reset-email", context);
    }

    public void sendIpChangeAlertEmail(String to, String subject, LoginAlertDto loginAlertDto) throws MessagingException {
        Context context = new Context();
        context.setVariable("logo", logoUrl);
        context.setVariable("username", loginAlertDto.getUser().getUsername());
        context.setVariable("os", loginAlertDto.getOs());
        context.setVariable("browser", loginAlertDto.getBrowser());
        context.setVariable("location", loginAlertDto.getCity() + ", " + loginAlertDto.getRegion());
        context.setVariable("country", loginAlertDto.getCountry());
        context.setVariable("formattedLoginTime", loginAlertDto.getFormattedLoginTime());
        context.setVariable("clientIp", loginAlertDto.getClientIp());
        context.setVariable("mapUrl", loginAlertDto.getMapUrl());
        context.setVariable("changePasswordUrl", frontendBaseUrl + "/change-password?email=" + loginAlertDto.getUser().getEmail());
        context.setVariable("deviceImagePath", loginAlertDto.getDeviceImagePath());
        sendEmail(to, subject, "login-alert-email", context);
    }
}
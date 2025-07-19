package com.multicloud.notificationservice.service;

import com.multicloud.commonlib.email.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender emailSender, SpringTemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(EmailNotification notification) throws MessagingException {
        EmailRequest request = notification.getEmailRequest();
        Context context = new Context();

        // Set common variables
        context.setVariable("logo", getLogoUrl(request));

        // Set type-specific variables
        switch(request.getType()) {
            case VERIFICATION:
                VerificationEmailRequest verRequest = (VerificationEmailRequest) request;
                context.setVariable("firstName", verRequest.getFirstName());
                context.setVariable("verificationCode", verRequest.getVerificationCode());
                break;
            case PASSWORD_RESET:
                PasswordResetEmailRequest resetRequest = (PasswordResetEmailRequest) request;
                context.setVariable("firstName", resetRequest.getFirstName());
                context.setVariable("resetLink", resetRequest.getResetLink());
                break;
            case LOGIN_ALERT:
                LoginAlertEmailRequest alertRequest = (LoginAlertEmailRequest) request;
                context.setVariable("username", alertRequest.getUsername());
                context.setVariable("os", alertRequest.getOs());
                context.setVariable("browser", alertRequest.getBrowser());
                context.setVariable("location", alertRequest.getLocation());
                context.setVariable("country", alertRequest.getCountry());
                context.setVariable("formattedLoginTime", alertRequest.getFormattedLoginTime());
                context.setVariable("clientIp", alertRequest.getClientIp());
                context.setVariable("mapUrl", alertRequest.getMapUrl());
                context.setVariable("changePasswordUrl", alertRequest.getChangePasswordUrl());
                context.setVariable("deviceImagePath", alertRequest.getDeviceImagePath());
                break;
        }

        String htmlContent = templateEngine.process(notification.getTemplateName(), context);
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

    private String getLogoUrl(EmailRequest request) {
        if (request instanceof VerificationEmailRequest verificationEmailRequest) {
            return verificationEmailRequest.getLogoUrl();
        } else if (request instanceof PasswordResetEmailRequest passwordResetEmailRequest) {
            return passwordResetEmailRequest.getLogoUrl();
        } else if (request instanceof LoginAlertEmailRequest loginAlertEmailRequest) {
            return loginAlertEmailRequest.getLogoUrl();
        }
        return "";
    }
}

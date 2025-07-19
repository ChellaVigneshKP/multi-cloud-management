package com.multicloud.notificationservice.component;

import com.multicloud.commonlib.email.EmailNotification;
import com.multicloud.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class EmailNotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationConsumer.class);

    private final EmailService emailService;

    public EmailNotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @Bean
    public Consumer<EmailNotification> authEmailNotification() {
        return emailNotification -> {
            try {
                logger.info("Sending email notification: {}", emailNotification);
                emailService.sendEmail(emailNotification);
            } catch (Exception e) {
                logger.error("Failed to send email notification", e);
            }
        };
    }
}

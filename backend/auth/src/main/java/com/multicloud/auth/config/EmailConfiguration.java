package com.multicloud.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration  // Indicates that this class contains Spring configuration
public class EmailConfiguration {

    @Value("${spring.mail.username}")  // Injects the email username from application properties
    private String emailUsername;

    @Value("${spring.mail.password}")  // Injects the email password from application properties
    private String emailPassword;

    // Bean to configure and provide JavaMailSender
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();  // Create a new JavaMailSenderImpl instance
        mailSender.setHost("smtp.gmail.com");  // Set the mail server host (Gmail)
        mailSender.setPort(587);  // Set the mail server port for TLS

        // Set the username and password for authentication
        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailPassword);

        // Configure additional mail properties
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");  // Set transport protocol
        props.put("mail.smtp.auth", "true");  // Enable SMTP authentication
        props.put("mail.smtp.starttls.enable", "true");  // Enable STARTTLS for secure connections
        props.put("mail.debug", "true");  // Enable debugging for mail sending (useful for troubleshooting)

        return mailSender;  // Return the configured JavaMailSender instance
    }
}

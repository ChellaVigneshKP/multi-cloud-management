package com.multicloud.auth.service;

import jakarta.mail.MessagingException;  // Exception for messaging errors
import jakarta.mail.internet.MimeMessage;  // Class for MIME messages
import org.springframework.beans.factory.annotation.Autowired;  // For dependency injection
import org.springframework.mail.javamail.JavaMailSender;  // Interface for sending emails
import org.springframework.mail.javamail.MimeMessageHelper;  // Helper class for creating MIME messages
import org.springframework.stereotype.Service;  // Indicates that this class is a service component

@Service  // Marks this class as a Spring service
public class EmailService {
    @Autowired  // Automatically injects the JavaMailSender bean
    private JavaMailSender emailSender;

    // Method to send a verification email
    public void sendVerificationEmail(String to, String subject, String text) throws MessagingException {
        // Create a new MIME message
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);  // Helper for setting message properties

        // Set the recipient, subject, and body of the email
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);  // Set the email content as HTML

        // Send the email
        emailSender.send(message);
    }
}

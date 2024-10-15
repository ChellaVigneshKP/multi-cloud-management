package com.multicloud.auth.service;

import com.multicloud.auth.model.User;
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

    public void sendPasswordResetEmail(User user) throws MessagingException {
        String to = user.getEmail();
        String subject = "Password Reset Request";
        String resetLink = "http://localhost:3000/reset-password?token=" + user.getPasswordResetToken(); // Adjust the frontend URL as needed

        String htmlContent = "<html>"
                + "<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;\">"
                + "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" style=\"border-collapse: collapse; background-color: #ffffff;\">"
                + "<tr>"
                + "<td align=\"center\" bgcolor=\"#B45C39\" style=\"padding: 40px 0 30px 0; color: #ffffff; font-size: 28px; font-weight: bold;\">"
                + "Password Reset Request"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td bgcolor=\"#ffffff\" style=\"padding: 40px 30px 40px 30px;\">"
                + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">"
                + "<tr>"
                + "<td style=\"color: #333333; font-size: 18px; padding-bottom: 20px;\">"
                + "Dear " + user.getFirstName() + ","
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"color: #333333; font-size: 16px; padding-bottom: 20px;\">"
                + "We received a request to reset the password for your account. Click the button below to reset your password:"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td align=\"center\" style=\"padding: 20px 0;\">"
                + "<a href=\"" + resetLink + "\" style=\"background-color: #B45C39; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-size: 18px;\">Reset Password</a>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"color: #333333; font-size: 16px; padding-bottom: 20px;\">"
                + "If you did not request a password reset, please ignore this email. Your password will remain unchanged."
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"color: #333333; font-size: 16px; padding-top: 30px;\">"
                + "Best regards,<br/><strong>KPCV Team</strong>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td bgcolor=\"#B45C39\" style=\"padding: 30px 30px 30px 30px;\">"
                + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">"
                + "<tr>"
                + "<td style=\"color: #ffffff; font-size: 14px; text-align: center;\">"
                + "If you have any questions, feel free to <a href=\"mailto:info@chellavignesh.com\" style=\"color: #ffffff; text-decoration: underline;\">contact us</a>."
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"color: #ffffff; font-size: 14px; text-align: center; padding-top: 10px;\">"
                + "&copy; 2024 chellavignesh.com. All rights reserved."
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</body>"
                + "</html>";

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        emailSender.send(message);
    }
}

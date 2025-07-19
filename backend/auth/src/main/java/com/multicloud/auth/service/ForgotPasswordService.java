package com.multicloud.auth.service;

import com.multicloud.auth.model.User;
import com.multicloud.auth.repository.UserRepository;
import com.multicloud.commonlib.exceptions.InvalidPasswordResetTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsyncEmailNotificationService asyncEmailNotificationService;

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordService.class);

    public ForgotPasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder, AsyncEmailNotificationService asyncEmailNotificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.asyncEmailNotificationService = asyncEmailNotificationService;
    }
    private String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[24];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    // **Handle Forgot Password Request**
    public void processForgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        // Generate a token even if the user does not exist (to simulate timing consistency)
        String token = generateToken();

        // If user doesn't exist, stop here (without throwing an error)
        if (userOptional.isEmpty()) {
            logger.debug("Failed to Send Mail to EmailID {} as the User with mail id doesn't exists", email);
            return;  // Return early for non-existing users
        }

        User user = userOptional.get();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour
        userRepository.save(user);
        // Send the email asynchronously
        asyncEmailNotificationService.producePasswordResetNotification(user);
    }

    // **Handle Reset Password Request**
    public void resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByPasswordResetToken(token);
        if (userOptional.isEmpty()) {
            throw new InvalidPasswordResetTokenException("Invalid or expired password reset token.");
        }

        User user = userOptional.get();
        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidPasswordResetTokenException("Password reset token has expired.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
        logger.info("Password reset successfully for User {}. The last login for the User was from {}", user.getUsername(), user.getLastLoginIp());
    }
}
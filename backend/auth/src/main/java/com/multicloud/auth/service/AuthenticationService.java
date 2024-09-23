package com.multicloud.auth.service;

import com.multicloud.auth.dto.LoginUserDto;
import com.multicloud.auth.dto.RegisterUserDto;
import com.multicloud.auth.dto.VerifyUserDto;
import com.multicloud.auth.exception.AccountNotVerifiedException;
import com.multicloud.auth.exception.EmailAlreadyRegisteredException;
import com.multicloud.auth.exception.UsernameAlreadyExistsException;
import com.multicloud.auth.exception.UsernameNotFoundException;
import com.multicloud.auth.model.User;
import com.multicloud.auth.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service  // Indicates that this class is a service component
public class AuthenticationService {
    private final UserRepository userRepository;  // Repository for user data
    private final PasswordEncoder passwordEncoder;  // Encoder for passwords
    private final AuthenticationManager authenticationManager;  // Manager for authentication
    private final EmailService emailService;  // Service for sending emails
    private final KafkaTemplate<String, Map<String, String>> kafkaTemplate;  // Kafka template for message production
    private static final String USER_REGISTRATION_TOPIC = "user-registration";  // Kafka topic for user registration
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);  // Logger for logging events

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            KafkaTemplate<String, Map<String, String>> kafkaTemplate
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Method for user registration
    public User signup(RegisterUserDto input) {
        // Check if the username already exists
        if(userRepository.existsByUsername(input.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already Exists");
        }
        // Check if the email is already registered
        if(userRepository.existsByEmail(input.getEmail())) {
            throw new EmailAlreadyRegisteredException("Email already registered");
        }
        // Create a new user and set verification properties
        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()), input.getFirstName(), input.getLastName());
        user.setVerificationCode(generateVerificationCode());  // Generate verification code
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));  // Set expiration time for verification code
        user.setEnabled(false);  // Set account as not enabled
        sendVerificationEmail(user);  // Send verification email
        User savedUser = userRepository.save(user);  // Save user to the database
        logger.info("New user registered with username: {}", user.getUsername());  // Log registration event
        return savedUser;  // Return the saved user
    }

    // Method for user authentication
    public User authenticate(LoginUserDto input) {
        // Retrieve user by email
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        // Check if the account is verified
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please verify your account.");
        }

        // Authenticate user with provided credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return user;  // Return authenticated user
    }

    // Method to verify user account
    public void verifyUser(VerifyUserDto input) {
        Map<String, String> userDetails = new HashMap<>();
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());  // Find user by email
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Check if the user is already verified
            if (user.isEnabled()) {
                throw new RuntimeException("User already verified");
            }
            // Check if the verification code has expired
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            // Verify the user's code
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);  // Enable the user account
                user.setVerificationCode(null);  // Clear the verification code
                user.setVerificationCodeExpiresAt(null);  // Clear expiration time
                userRepository.save(user);  // Save the updated user
                userDetails.put("email", user.getEmail());
                userDetails.put("username", user.getUsername());
                logger.info("User with username '{}' verified successfully", user.getUsername());
                // Send user details to Kafka
                logger.info("Sending to Kafka - Topic: {}, Key (Email): {}, Value (Username): {}", USER_REGISTRATION_TOPIC, user.getUsername(), userDetails);
                kafkaTemplate.send(USER_REGISTRATION_TOPIC, user.getUsername(), userDetails);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // Method to resend verification code
    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);  // Find user by email
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // Check if the account is already verified
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());  // Generate a new verification code
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));  // Set new expiration time
            sendVerificationEmail(user);  // Send verification email
            userRepository.save(user);  // Save updated user
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // Method to send verification email
    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";  // Email subject
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();  // Verification code message
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);  // Send the email
            logger.info("Email sent successfully to mail id '{}'", user.getEmail());
        } catch (MessagingException e) {
            // Handle email sending exception
            logger.error("Failed to send verification email", e);
        }
    }

    // Method to generate a random verification code
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;  // Generate a 6-digit code
        return String.valueOf(code);
    }

    // Method to load user by username
    public User loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found with username: " + username));
    }
}

package com.multicloud.auth.config;

import com.multicloud.auth.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration  // Indicates that this class contains Spring configuration
public class ApplicationConfiguration {
    private final UserRepository userRepository;  // Repository for accessing user data

    // Constructor to inject the UserRepository dependency
    public ApplicationConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Bean for UserDetailsService to load user-specific data
    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)  // Find user by email
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));  // Throw exception if user not found
    }

    // Bean for password encoder using BCrypt
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Returns a BCryptPasswordEncoder instance
    }

    // Bean for AuthenticationManager to handle authentication processes
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();  // Retrieves the authentication manager from the configuration
    }

    // Bean for AuthenticationProvider using DaoAuthenticationProvider
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();  // Create a new DaoAuthenticationProvider

        authProvider.setUserDetailsService(userDetailsService());  // Set the user details service
        authProvider.setPasswordEncoder(passwordEncoder());  // Set the password encoder

        return authProvider;  // Return the configured authentication provider
    }
}

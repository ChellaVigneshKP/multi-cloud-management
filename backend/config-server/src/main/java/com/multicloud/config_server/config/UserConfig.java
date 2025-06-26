package com.multicloud.config_server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
public class UserConfig {
    @Value("${spring.security.default.user.name}")
    private String defaultUserName;

    @Value("${spring.security.default.user.password}")
    private String defaultUserPassword;

    @Value("${spring.security.advanced.user.name}")
    private String advancedUserName;

    @Value("${spring.security.advanced.user.password}")
    private String advancedUserPassword;

    @Value("${spring.security.advanced.user.roles}")
    private String advancedUserRoles;

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails adminUser = User.builder()
                .username(advancedUserName)
                .password(passwordEncoder().encode(advancedUserPassword)) // Securely encode password
                .roles(advancedUserRoles) // Assign ADMINSERVER role
                .build();

        UserDetails regularUser = User.builder()
                .username(defaultUserName)
                .password(passwordEncoder().encode(defaultUserPassword)) // Securely encode password
                .roles("USER") // Assign USER role
                .build();

        return new InMemoryUserDetailsManager(adminUser, regularUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
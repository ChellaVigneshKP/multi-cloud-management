package com.multicloud.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration  // Marks this class as a configuration class for Spring
@EnableWebSecurity  // Enables Spring Security’s web security support
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;  // Provider for authentication
    private final JwtAuthenticationFilter jwtAuthenticationFilter;  // Custom JWT authentication filter

    public SecurityConfiguration(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider
    ) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean  // Indicates that this method produces a bean to be managed by the Spring container
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF protection for stateless APIs
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Apply CORS configuration
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**").permitAll()  // Allow public access to authentication endpoints
                        .anyRequest().authenticated()  // Require authentication for all other requests
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Set session management to stateless
                )
                .authenticationProvider(authenticationProvider)  // Set the custom authentication provider
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // Add the JWT filter before the username/password filter

        return http.build();  // Build and return the security filter chain
    }

    @Bean  // Indicates that this method produces a bean to be managed by the Spring container
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));  // Allow specified origin (adjust as needed)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));  // Allow specified HTTP methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));  // Allow specified headers
        configuration.setAllowCredentials(true);  // Allow credentials (cookies, authorization headers, etc.)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Register CORS configuration for all paths
        return source;  // Return the configured CORS source
    }
}

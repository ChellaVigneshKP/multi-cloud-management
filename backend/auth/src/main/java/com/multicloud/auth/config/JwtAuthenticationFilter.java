package com.multicloud.auth.config;

import com.multicloud.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component  // Indicates that this class is a Spring component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;  // To handle exceptions

    private final JwtService jwtService;  // Service for JWT operations
    private final UserDetailsService userDetailsService;  // Service for loading user details

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,  // HTTP request
            @NonNull HttpServletResponse response, // HTTP response
            @NonNull FilterChain filterChain // Filter chain for processing requests
    ) throws ServletException, IOException {
        // Get the Authorization header from the request
        final String authHeader = request.getHeader("Authorization");

        // Check if the header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // If not, continue the filter chain
            return;
        }

        try {
            // Extract the JWT token from the header
            final String jwt = authHeader.substring(7);  // Remove "Bearer " prefix
            final String userEmail = jwtService.extractUsername(jwt);  // Extract username from token

            // Get the current authentication from the Security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // If userEmail is present and no authentication exists
            if (userEmail != null && authentication == null) {
                // Load user details from the UserDetailsService
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validate the token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authentication token for the user
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()  // Set the user's authorities
                    );

                    // Set the request details in the authentication token
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Set the authentication in the Security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // Continue the filter chain
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            // Handle any exceptions that occur during filtering
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}

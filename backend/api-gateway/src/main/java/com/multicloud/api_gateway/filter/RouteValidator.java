package com.multicloud.api_gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;  // Represents the server HTTP request
import org.springframework.stereotype.Component;  // Indicates that this class is a Spring component

import java.util.List;  // For using List collection
import java.util.function.Predicate;  // For using functional predicates

@Component  // Marks this class as a Spring-managed bean
public class RouteValidator {

    // List of open API endpoints that do not require authentication
    public static final List<String> openApiEndpoints = List.of(
            "/auth/signup",        // Endpoint for user signup
            "/eureka",             // Endpoint for service discovery (Eureka)
            "/auth/verify",        // Endpoint for verifying user accounts
            "/auth/login",         // Endpoint for user login
            "/auth/resend",        // Endpoint for resending verification codes
            "/auth/validate-token", // Endpoint for validating JWT tokens
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/take-action",
            "/auth/v3/**",
            "/auth/swagger-ui/**",
            "/favicon.ico",
            "/auth/refresh-token",
            "/auth/logout"
    );

    // Predicate to determine if a request is secured (requires authentication)
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints.stream()
                    .noneMatch(url -> {
                        String path = request.getURI().getPath();
                        return path.startsWith(url.replace("/**", ""));  // Handles wildcard properly
                    }); // Check if the request path starts with any open API endpoint
}

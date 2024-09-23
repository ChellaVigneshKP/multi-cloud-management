package com.multicloud.api_gateway.filter;

import com.multicloud.api_gateway.util.JwtUtil;  // Utility for JWT operations
import org.springframework.beans.factory.annotation.Autowired;  // For dependency injection
import org.springframework.cloud.gateway.filter.GatewayFilter;  // Interface for creating gateway filters
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;  // Base class for creating filters
import org.springframework.core.io.buffer.DataBuffer;  // For handling data buffers in responses
import org.springframework.http.HttpHeaders;  // For HTTP header constants
import org.springframework.http.HttpStatus;  // For representing HTTP status codes
import org.springframework.http.MediaType;  // For media type definitions
import org.springframework.http.server.reactive.ServerHttpResponse;  // Represents server HTTP response
import org.springframework.stereotype.Component;  // Indicates that this class is a Spring component
import reactor.core.publisher.Mono;  // For reactive programming support

@Component  // Marks this class as a Spring-managed bean
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;  // Validator to check if routes require authentication

    @Autowired
    private JwtUtil jwtUtil;  // Utility for JWT validation

    // Constructor for the filter factory, specifying the configuration class
    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Returns a GatewayFilter that processes requests
        return ((exchange, chain) -> {
            // Check if the request is for a secured route
            if (validator.isSecured.test(exchange.getRequest())) {
                // Check for the Authorization header
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Missing authorization header");
                }

                // Retrieve the Authorization header
                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

                // Validate the Bearer token format
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);  // Remove "Bearer " prefix
                } else {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Invalid authorization header format");
                }

                // Validate the JWT token
                try {
                    jwtUtil.validateToken(authHeader);
                } catch (InvalidJwtTokenException e) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Invalid JWT token");
                } catch (Exception e) {
                    return handleException(exchange.getResponse(), HttpStatus.INTERNAL_SERVER_ERROR, "Token validation error");
                }
            }
            // Proceed with the next filter in the chain
            return chain.filter(exchange);
        });
    }

    // Handles exceptions by returning an error response
    private Mono<Void> handleException(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);  // Set the HTTP status code
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);  // Set the response content type
        String responseBody = "{\"error\": \"" + message + "\"}";  // Create the error response body
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());  // Wrap the response body in a data buffer
        return response.writeWith(Mono.just(buffer));  // Write the buffer to the response
    }

    // Configuration class for the filter
    public static class Config {

    }

    // Custom exception for invalid JWT tokens
    public class InvalidJwtTokenException extends RuntimeException {
        public InvalidJwtTokenException(String message) {
            super(message);  // Call the superclass constructor
        }
    }
}

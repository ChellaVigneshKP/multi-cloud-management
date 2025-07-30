package com.multicloud.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

import static com.multicloud.commonlib.constants.gateway.Constants.OPEN_API_ENDPOINTS;

@Component  // Marks this class as a Spring-managed bean
public class RouteValidator {
    private RouteValidator() {
        // Prevent instantiation
    }

    // Predicate to determine if a request is secured (requires authentication)
    public static final Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_API_ENDPOINTS.stream()
                    .noneMatch(url -> {
                        String path = request.getURI().getPath();
                        return path.startsWith(url.replace("/**", ""));  // Handles wildcard properly
                    }); // Check if the request path starts with any open API endpoint

    public static Predicate<ServerHttpRequest> getIsSecured() {
        return isSecured;
    }
}

package com.multicloud.api_gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public static final List<String> openApiEndpoints = List.of(
            "/auth/signup",
            "/eureka",
            "/auth/verify",
            "/auth/login",
            "/auth/resend",
            "/auth/validate-token"
    );
    public Predicate<ServerHttpRequest> isSecured=
            request -> openApiEndpoints.stream().noneMatch(url -> request.getURI().getPath().startsWith(url));
}
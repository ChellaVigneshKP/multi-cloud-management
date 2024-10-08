package com.multicloud.api_gateway.filter;

import com.multicloud.api_gateway.util.JweUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JweUtil jweUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getURI().getPath();
            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Missing authorization header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                } else {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Invalid authorization header format");
                }

                try {
                    JWTClaimsSet claims = jweUtil.validateToken(authHeader);
                    // Extract user details from claims
                    String username = claims.getSubject();
                    String email = (String) claims.getClaim("emailId");
                    String userId = String.valueOf(claims.getClaim("userId"));

                    // Extract client IP addresses
                    String[] clientIpAddresses = extractClientIpAddresses(request);
                    String ipAddressV4 = clientIpAddresses[0];
                    String ipAddressV6 = clientIpAddresses[1];

                    // Log the IP addresses and user information
                    logger.info("Request from IPv4: {}, IPv6: {} to Path: {}", ipAddressV4, ipAddressV6, requestPath);
                    logger.info("Request from IPv4: {}, IPv6: {}, Username: {}, Email: {}, UserId: {}", ipAddressV4, ipAddressV6, username, email, userId);

                    // Remove existing headers to prevent header injection
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .headers(httpHeaders -> {
                                httpHeaders.remove("X-User-Name");
                                httpHeaders.remove("X-User-Email");
                                httpHeaders.remove("X-User-Id");
                            })
                            .header("X-User-Name", username)
                            .header("X-User-Email", email)
                            .header("X-User-Id", userId)
                            .build();
                    // Continue the filter chain with the modified request
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (Exception e) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Invalid JWE token");
                }
            } else {
                String[] clientIpAddresses = extractClientIpAddresses(request);
                String ipAddressV4 = clientIpAddresses[0];
                String ipAddressV6 = clientIpAddresses[1];
                logger.info("Request to unsecured path from IPv4: {}, IPv6: {} to Path: {}", ipAddressV4, ipAddressV6, requestPath);
            }
            return chain.filter(exchange);
        };
    }

    private String[] extractClientIpAddresses(ServerHttpRequest request) {
        String ipAddressV4 = "Unknown";
        String ipAddressV6 = "Unknown";

        // Try to get the IP from X-Forwarded-For header
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IP addresses in case of proxy chaining
            String[] ipAddresses = xForwardedFor.split(",");
            String clientIp = ipAddresses[0].trim();
            try {
                InetAddress inetAddress = InetAddress.getByName(clientIp);
                if (inetAddress instanceof java.net.Inet4Address) {
                    ipAddressV4 = inetAddress.getHostAddress();
                } else if (inetAddress instanceof java.net.Inet6Address) {
                    ipAddressV6 = inetAddress.getHostAddress();
                    // Check for IPv4-mapped IPv6 address
                    if (ipAddressV6.startsWith("::ffff:")) {
                        ipAddressV4 = ipAddressV6.substring(7);
                    }
                }
            } catch (Exception e) {
                logger.error("Error parsing IP address from X-Forwarded-For: {}", e.getMessage());
            }
        } else {
            // Fallback to remote address
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (remoteAddress != null) {
                InetAddress inetAddress = remoteAddress.getAddress();
                if (inetAddress instanceof java.net.Inet4Address) {
                    ipAddressV4 = inetAddress.getHostAddress();
                } else if (inetAddress instanceof java.net.Inet6Address) {
                    ipAddressV6 = inetAddress.getHostAddress();
                    // Check for IPv4-mapped IPv6 address
                    if (ipAddressV6.startsWith("::ffff:")) {
                        ipAddressV4 = ipAddressV6.substring(7);
                    }
                }
            }
        }
        return new String[]{ipAddressV4, ipAddressV6};
    }

    private Mono<Void> handleException(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String responseBody = "{\"error\": \"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuration properties if needed
    }
}

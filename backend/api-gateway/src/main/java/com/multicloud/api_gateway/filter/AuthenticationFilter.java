package com.multicloud.api_gateway.filter;

import com.multicloud.api_gateway.exception.TokenExpiredException;
import com.multicloud.api_gateway.util.IpAddressUtil;
import com.multicloud.api_gateway.util.JweUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.multicloud.commonlib.constants.AuthConstants.JWE_TOKEN_COOKIE_NAME;
import static com.multicloud.commonlib.constants.DeviceConstants.*;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JweUtil jweUtil;

    public AuthenticationFilter(JweUtil jweUtil) {
        super(Config.class);
        this.jweUtil = jweUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getURI().getPath();
            if (RouteValidator.getIsSecured().test(request)) {
                HttpCookie jweTokenCookie = request.getCookies().getFirst(JWE_TOKEN_COOKIE_NAME);
                String jweToken = jweTokenCookie != null ? jweTokenCookie.getValue() : null;
                if (jweToken == null) {
                    return handleException(exchange.getResponse(), "Missing authorization cookie");
                }
                try {
                    JWTClaimsSet claims = jweUtil.validateToken(jweToken);
                    // Extract user details from claims
                    String username = claims.getSubject();
                    String email = (String) claims.getClaim("emailId");
                    String userId = String.valueOf(claims.getClaim("userId"));
                    // Extract client IP addresses
                    String[] clientIpAddresses = IpAddressUtil.resolveClientIps(request);
                    String ipAddressV4 = clientIpAddresses[0];
                    String ipAddressV6 = clientIpAddresses[1];

                    // Log the IP addresses and user information
                    logger.info("Request from IPv4: {}, IPv6: {} to Path: {}", ipAddressV4, ipAddressV6, requestPath);
                    logger.info("Request from IPv4: {}, IPv6: {}, Username: {}, Email: {}, UserId: {}", ipAddressV4, ipAddressV6, username, email, userId);

                    // Create a modified request with additional headers
                    ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public @NonNull HttpHeaders getHeaders() {
                            HttpHeaders headers = super.getHeaders();
                            headers.set(X_USER_NAME, username);
                            headers.set(X_USER_EMAIL, email);
                            headers.set(X_USER_ID, userId);
                            headers.set(HEADER_IPV4, ipAddressV4);
                            headers.set(HEADER_IPV6, ipAddressV6);
                            return headers;
                        }
                    };
                    // Continue the filter chain with the modified request
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (TokenExpiredException e) {
                    return handleException(exchange.getResponse(), "Token has expired");
                } catch (Exception e) {
                    return handleException(exchange.getResponse(), "Invalid JWE token");
                }
            } else {
                String[] clientIpAddresses = IpAddressUtil.resolveClientIps(request);
                String ipAddressV4 = clientIpAddresses[0];
                String ipAddressV6 = clientIpAddresses[1];
                logger.info("Request to unsecured path from IPv4: {}, IPv6: {} to Path: {}", ipAddressV4, ipAddressV6, requestPath);
                // Create a modified request with additional headers
                ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public @NonNull HttpHeaders getHeaders() {
                        HttpHeaders headers = super.getHeaders();
                        headers.set(HEADER_IPV4, ipAddressV4);
                        headers.set(HEADER_IPV6, ipAddressV6);
                        return headers;
                    }
                };
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
        };
    }

    private Mono<Void> handleException(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String responseBody = "{\"error\": \"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        private boolean enableLogging; // Example property to enable logging
        private String customHeader;   // Example property for a custom header

        // Constructor
        public Config(boolean enableLogging, String customHeader) {
            this.enableLogging = enableLogging;
            this.customHeader = customHeader != null ? customHeader : "";
        }

        // Getters and setters
        public boolean isEnableLogging() {
            return enableLogging;
        }

        public void setEnableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
        }

        public String getCustomHeader() {
            return customHeader;
        }

        public void setCustomHeader(String customHeader) {
            this.customHeader = customHeader;
        }
    }
}

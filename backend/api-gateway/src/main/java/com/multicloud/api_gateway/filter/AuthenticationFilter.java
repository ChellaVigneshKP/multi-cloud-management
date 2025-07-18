package com.multicloud.api_gateway.filter;

import com.multicloud.api_gateway.exception.TokenExpiredException;
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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JweUtil jweUtil;
    private static final String UNKNOWN_IP = "Unknown";
    private static final String NOT_APPLICABLE = "N/A";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";

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
                HttpCookie jweTokenCookie = request.getCookies().getFirst("jweToken");
                String jweToken = jweTokenCookie != null ? jweTokenCookie.getValue() : null;
                if (jweToken == null) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Missing authorization cookie");
                }
                try {
                    JWTClaimsSet claims = jweUtil.validateToken(jweToken);
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

                    // Create a modified request with additional headers
                    ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public @NonNull HttpHeaders getHeaders() {
                            HttpHeaders headers = super.getHeaders();
                            headers.set("X-User-Name", username);
                            headers.set("X-User-Email", email);
                            headers.set("X-User-Id", userId);
                            headers.set("X-User-IP", ipAddressV4);
                            headers.set("X-User-IP-V6", ipAddressV6);
                            return headers;
                        }
                    };
                    // Continue the filter chain with the modified request
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());

                } catch (TokenExpiredException e) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Token has expired");
                } catch (Exception e) {
                    return handleException(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Invalid JWE token");
                }
            } else {
                String[] clientIpAddresses = extractClientIpAddresses(request);
                String ipAddressV4 = clientIpAddresses[0];
                String ipAddressV6 = clientIpAddresses[1];
                logger.info("Request to unsecured path from IPv4: {}, IPv6: {} to Path: {}", ipAddressV4, ipAddressV6, requestPath);
                // Create a modified request with additional headers
                ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public @NonNull HttpHeaders getHeaders() {
                        HttpHeaders headers = super.getHeaders();
                        headers.set("X-User-IP", ipAddressV4);
                        headers.set("X-User-IP-V6", ipAddressV6);
                        return headers;
                    }
                };
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
        };
    }

    private String[] extractClientIpAddresses(ServerHttpRequest request) {
        String ipAddressV4 = UNKNOWN_IP;
        String ipAddressV6 = UNKNOWN_IP;

        // Get all potential IP sources
        String ipToCheck = Stream.of(
                        request.getHeaders().getFirst(X_FORWARDED_FOR),
                        request.getHeaders().getFirst(X_REAL_IP),
                        request.getRemoteAddress() != null ?
                                request.getRemoteAddress().getAddress().getHostAddress() : null
                )
                .filter(ip -> ip != null && !ip.isEmpty())
                .findFirst()
                .map(ip -> ip.split(",")[0].trim())
                .orElse(null);

        // Parse the IP if found
        if (ipToCheck != null) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ipToCheck);
                if (inetAddress instanceof Inet4Address) {
                    ipAddressV4 = inetAddress.getHostAddress();
                    ipAddressV6 = NOT_APPLICABLE;
                }
                else if (inetAddress instanceof Inet6Address) {
                    ipAddressV6 = inetAddress.getHostAddress();
                    ipAddressV4 = ipAddressV6.startsWith("::ffff:") ?
                            ipAddressV6.substring(7) : NOT_APPLICABLE;
                }
            }
            catch (UnknownHostException e) {
                logger.debug("Invalid IP address format '{}'", ipToCheck);
            }
            catch (Exception e) {
                logger.error("Unexpected error parsing IP '{}'", ipToCheck, e);
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

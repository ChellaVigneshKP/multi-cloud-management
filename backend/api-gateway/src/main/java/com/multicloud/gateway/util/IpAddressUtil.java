package com.multicloud.gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import static com.multicloud.commonlib.constants.DeviceConstants.*;
import static com.multicloud.commonlib.constants.gateway.Constants.IP_HEADER_CANDIDATES;

public class IpAddressUtil {
    private IpAddressUtil() {
        // Utility class, prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final Pattern FORWARDED_HEADER_PATTERN =
            Pattern.compile("for=([^;,\\s]+)", Pattern.CASE_INSENSITIVE);

    public static String[] resolveClientIps(ServerHttpRequest request) {
        String clientIp = null;

        // Check headers first
        for (String header : IP_HEADER_CANDIDATES) {
            String headerValue = request.getHeaders().getFirst(header);
            if (headerValue != null && !headerValue.isBlank()) {
                if (header.equalsIgnoreCase("Forwarded")) {
                    // Special handling for Forwarded header (RFC 7239)
                    var matcher = FORWARDED_HEADER_PATTERN.matcher(headerValue);
                    if (matcher.find()) {
                        clientIp = matcher.group(1).trim();
                    }
                } else {
                    clientIp = headerValue.split(",")[0].trim();
                }

                if (clientIp != null && !clientIp.isEmpty()) {
                    break;
                }
            }
        }

        // Fall back to a remote address
        if ((clientIp == null || clientIp.isEmpty())
                && request.getRemoteAddress() != null
                && request.getRemoteAddress().getAddress() != null) {
            clientIp = request.getRemoteAddress().getAddress().getHostAddress();
        }

        // Default values
        String ipV4 = UNKNOWN_IP;
        String ipV6 = UNKNOWN_IP;

        if (clientIp != null && !clientIp.isEmpty()) {
            try {
                // Handle cases where the IP might be enclosed in brackets (e.g., [::1])
                String cleanIp = clientIp.replaceAll("[\\[\\]]", "");

                InetAddress inetAddress = InetAddress.getByName(cleanIp);

                if (inetAddress.getHostAddress().contains(":")) {
                    // IPv6 address
                    if (inetAddress.getHostAddress().startsWith("::ffff:")) {
                        // IPv4-mapped IPv6
                        ipV4 = inetAddress.getHostAddress().substring(7);
                        ipV6 = NOT_APPLICABLE;
                    } else {
                        // Pure IPv6
                        ipV6 = inetAddress.getHostAddress();
                        ipV4 = NOT_APPLICABLE;
                    }
                } else {
                    // IPv4 address
                    ipV4 = inetAddress.getHostAddress();
                    ipV6 = NOT_APPLICABLE;
                }
            } catch (UnknownHostException e) {
                ipV4 = ipV6 = INVALID_IP;
            }
        }

        return new String[]{ipV4, ipV6};
    }
}
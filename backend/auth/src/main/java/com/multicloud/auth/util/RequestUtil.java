package com.multicloud.auth.util;

import com.multicloud.commonlib.constants.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {
    private RequestUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Retrieves the client IP address from the request headers.
     * It checks for both IPv4 and IPv6 addresses, preferring IPv4 if available.
     * It checks for both IPv4 and IPv6 addresses, preferring IPv4 if available.
     *
     * @param request the HttpServletRequest object
     * @return the client IP address as a String, or "UNKNOWN" if not found
     */
    public static String getClientIp(HttpServletRequest request) {
        String clientIpv4 = request.getHeader("X-User-IP");
        String clientIpv6 = request.getHeader("X-User-IP-V6");
        String clientIp = "UNKNOWN";
        if (!AuthConstants.NOT_APPLICABLE.equals(clientIpv4)) {
            clientIp = clientIpv4;  // Prefer IPv4 if it's available and not "Unknown"
        } else if (!AuthConstants.NOT_APPLICABLE.equals(clientIpv6)) {
            clientIp = clientIpv6;  // Fallback to IPv6 if IPv4 is "Unknown"
        }
        return clientIp;
    }

    public static boolean isRequestSecure(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedSsl = request.getHeader("X-Forwarded-Ssl");
        String frontEndHttps = request.getHeader("Front-End-Https");

        return request.isSecure()
                || "https".equalsIgnoreCase(forwardedProto)
                || "on".equalsIgnoreCase(forwardedSsl)
                || "on".equalsIgnoreCase(frontEndHttps);
    }
}

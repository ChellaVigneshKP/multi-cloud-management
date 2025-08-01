package com.multicloud.auth.util;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.multicloud.commonlib.constants.DeviceConstants.UNKNOWN_DEVICE;

public class UserAgentParser {
    private static final Logger logger = LoggerFactory.getLogger(UserAgentParser.class);

    private UserAgentParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String[] parseUserAgent(String userAgentString) {
        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
        Browser browser = userAgent.getBrowser();
        OperatingSystem os = userAgent.getOperatingSystem();
        DeviceType deviceType = os.getDeviceType();
        String browserName = browser.getName() != null ? browser.getName() : "Unknown Browser";
        String osName = os.getName() != null ? os.getName() : "Unknown OS";
        String deviceName = deviceType != null ? deviceType.getName() : "Unknown Device";
        logger.info("Browser: {}, OS: {}, Device: {}", browserName, osName, deviceName);
        return new String[]{browserName, osName, deviceName};
    }

    public static String buildDeviceInfo(String userAgent, HttpServletRequest request) {
        try {
            String[] parts = parseUserAgent(userAgent);
            if (parts.length < 3) return UNKNOWN_DEVICE;
            String screenResolution = request.getHeader("X-Screen-Resolution");
            String timezone = request.getHeader("X-Timezone-Offset");
            logger.debug("Screen resolution: {}, Timezone: {}", screenResolution, timezone);
            return String.format("%s:%s:%s", parts[2], parts[1], parts[0]);
        } catch (Exception e) {
            logger.warn("Failed to parse user agent: {}", userAgent, e);
            return UNKNOWN_DEVICE;
        }
    }
}
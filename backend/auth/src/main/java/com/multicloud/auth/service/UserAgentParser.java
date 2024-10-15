package com.multicloud.auth.service;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAgentParser {
    // Private constructor to prevent instantiation
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
        return new String[]{browserName, osName, deviceName};
    }
}
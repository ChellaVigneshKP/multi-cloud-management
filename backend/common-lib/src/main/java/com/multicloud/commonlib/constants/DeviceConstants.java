package com.multicloud.commonlib.constants;

/**
 * DeviceConstants.java
 * This class defines constants related to device types, operating systems, and browsers.
 * It is designed to be a utility class and should not be instantiated.
 */
public class DeviceConstants {
    /**
     * Private constructor to prevent instantiation.
     * This class is a utility class and should not be instantiated.
     */
    private DeviceConstants() {
        // public constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Constants for device types, operating systems, and browsers.
     */
    public static final String DEVICE_TYPE_DESKTOP = "Desktop";
    /**
     * Device types
     */
    public static final String DEVICE_TYPE_MOBILE = "Mobile";
    /**
     * Device types
     */
    public static final String DEVICE_TYPE_TABLET = "Tablet";
    /**
     * Device types
     */
    public static final String DEVICE_TYPE_UNKNOWN = "Unknown";
    /**
     * Device types
     */
    public static final String DEVICE_OS_WINDOWS = "Windows";
    /**
     * Device operating systems
     */
    public static final String DEVICE_OS_MAC = "Mac";
    /**
     * Device operating systems
     */
    public static final String DEVICE_OS_LINUX = "Linux";
    /**
     * Device operating systems
     */
    public static final String DEVICE_OS_ANDROID = "Android";
    /**
     * Device operating systems
     */
    public static final String DEVICE_OS_IOS = "iOS";
    /**
     * Device operating systems
     */
    public static final String DEVICE_OS_UNKNOWN = "Unknown";
    /**
     * Device Browser
     */
    public static final String DEVICE_BROWSER_CHROME = "Chrome";
    /**
     * Device Browser
     */
    public static final String DEVICE_BROWSER_FIREFOX = "Firefox";
    /**
     * Device Browser
     */
    public static final String DEVICE_BROWSER_SAFARI = "Safari";
    /**
     * Device Browser
     */
    public static final String DEVICE_BROWSER_EDGE = "Edge";
    /**
     * Device Browser
     */
    public static final String DEVICE_BROWSER_OPERA = "Opera";
    /**
     * Device Browser
     */
    public static final String DEVICE_BROWSER_UNKNOWN = "Unknown";
    /**
     * Device Identity
     */
    public static final String UNKNOWN_DEVICE = "unknown:unknown:unknown";
    /**
     * Header for Timezone
     */
    public static final String HEADER_TIMEZONE = "X-Timezone";
    /**
     * Header for Device Screen Resolution
     */
    public static final String HEADER_SCREEN_RES = "X-Screen-Resolution";
    /**
     * Header for Timezone Offset
     */
    public static final String HEADER_TIMEZONE_OFFSET = "X-Timezone-Offset";
    /**
     * Header for HSTS (HTTP Strict Transport Security)
     */
    public static final String HSTS_HEADER_VALUE = "max-age=31536000; includeSubDomains; preload";
    /**
     * Header for Ipv4
     */
    public static final String HEADER_IPV4 = "X-User-IP";
    /**
     * Header for Ipv6
     */
    public static final String HEADER_IPV6 = "X-User-IP-V6";
    /**
     * Header for Unknown IP
     */
    public static final String UNKNOWN_IP = "Unknown";
    /**
     * Header for Real Ip
     */
    public static final String HEADER_REAL_IP = "X-Real-IP";
    /**
     * Header for Forwarded For
     */
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    /**
     * Header for Username
     */
    public static final String X_USER_NAME = "X-User-Name";
    /**
     * Header for User Email
     */
    public static final String X_USER_EMAIL = "X-User-Email";
    /**
     * Header for User Id
     */
    public static final String X_USER_ID = "X-User-Id";


}

package com.multicloud.commonlib.constants;


public class DeviceConstants {
    private DeviceConstants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Constants for device types, operating systems, and browsers.
     */
    public static final String DEVICE_TYPE_DESKTOP = "Desktop";
    public static final String DEVICE_TYPE_MOBILE = "Mobile";
    public static final String DEVICE_TYPE_TABLET = "Tablet";
    public static final String DEVICE_TYPE_UNKNOWN = "Unknown";
    public static final String DEVICE_OS_WINDOWS = "Windows";
    public static final String DEVICE_OS_MAC = "Mac";
    public static final String DEVICE_OS_LINUX = "Linux";
    public static final String DEVICE_OS_ANDROID = "Android";
    public static final String DEVICE_OS_IOS = "iOS";
    public static final String DEVICE_OS_UNKNOWN = "Unknown";
    public static final String DEVICE_BROWSER_CHROME = "Chrome";
    public static final String DEVICE_BROWSER_FIREFOX = "Firefox";
    public static final String DEVICE_BROWSER_SAFARI = "Safari";
    public static final String DEVICE_BROWSER_EDGE = "Edge";
    public static final String DEVICE_BROWSER_OPERA = "Opera";
    public static final String DEVICE_BROWSER_UNKNOWN = "Unknown";
    public static final String UNKNOWN_DEVICE = "unknown:unknown:unknown";


}

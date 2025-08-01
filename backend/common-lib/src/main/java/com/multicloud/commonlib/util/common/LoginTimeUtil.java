package com.multicloud.commonlib.util.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Utility class for formatting login times in a user-friendly way.
 * It converts UTC login times to the user's timezone and formats them for display.
 */
public class LoginTimeUtil {

    /**
     * Private constructor to prevent instantiation.
     * This class is a utility class and should not be instantiated.
     */
    private LoginTimeUtil() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    /**
     * DateTimeFormatter for formatting login time.
     * Example format: "July 23 at 06:15 PM IST"
     */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMMM dd 'at' hh:mm a z");

    /**
     * Converts LocalDateTime in UTC to the given timezone and formats it for display.
     *
     * @param utcDateTime The login time in UTC.
     * @param timezoneId The user's timezone (e.g., "Asia/Kolkata"). Falls back to UTC if null.
     * @return A formatted string like "July 23 at 06:15 PM IST".
     */
    public static String formatLoginTime(LocalDateTime utcDateTime, String timezoneId) {
        ZonedDateTime zonedDateTime = getZonedLoginTime(utcDateTime, timezoneId);
        return zonedDateTime.format(FORMATTER);
    }

    /**
     * Converts UTC time to ZonedDateTime in the specified zone.
     *
     * @param utcDateTime The UTC login time.
     * @param timezoneId The desired time zone.
     * @return ZonedDateTime in the target time zone.
     */
    public static ZonedDateTime getZonedLoginTime(LocalDateTime utcDateTime, String timezoneId) {
        ZoneId zone = ZoneId.of(Optional.ofNullable(timezoneId).orElse("UTC"));
        return utcDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(zone);
    }
}

package com.multicloud.commonlib.util.common;

/**
 * Utility class for generating Google Maps Static API URLs.
 * This class provides methods to create URLs for static maps with customizable parameters.
 */
public class MapUrlUtil {
    /**
     * Private constructor to prevent instantiation.
     * This class is a utility class and should not be instantiated.
     */
    private MapUrlUtil() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Base URL for the Google Maps Static API.
     */
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/staticmap";

    /**
     * Generates a Google Maps Static API URL with the specified parameters.
     *
     * @param location   The location to center the map on, in "latitude,longitude" format.
     * @param apiKey     Your Google Maps API key.
     * @param zoom       The zoom level of the map (1-21).
     * @param size       The size of the map image in "widthxheight" format (e.g., "600x300").
     * @param markerColor The color of the marker in hexadecimal format (e.g., "red", "blue").
     * @return A formatted URL string for the static map.
     */
    public static String generateMapUrl(String location, String apiKey, int zoom, String size, String markerColor) {
        return BASE_URL + "?center=" + location
                + "&zoom=" + zoom
                + "&size=" + size
                + "&maptype=roadmap"
                + "&markers=color:" + markerColor + "%7C" + location
                + "&key=" + apiKey;
    }

    /**
     * Generates a Google Maps Static API URL with default parameters.
     * This method uses a zoom level of 13, size of "600x300", and a red marker.
     *
     * @param location The location to center the map on, in "latitude,longitude" format.
     * @param apiKey   Your Google Maps API key.
     * @return A formatted URL string for the static map with default parameters.
     */
    public static String generateMapUrl(String location, String apiKey) {
        return generateMapUrl(location, apiKey, 13, "600x300", "red");
    }
}

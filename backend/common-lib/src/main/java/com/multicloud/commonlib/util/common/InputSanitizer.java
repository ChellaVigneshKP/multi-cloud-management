package com.multicloud.commonlib.util.common;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Utility class for input sanitization.
 * This class provides methods to sanitize user inputs to prevent security vulnerabilities such as XSS and SQL injection.
 * It is designed to be used across different modules of the application.
 */
public class InputSanitizer {
    /**
     * A policy factory that defines the sanitization rules.
     * This factory is used to create a policy that allows safe HTML while removing potentially harmful elements.
     */
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder().toFactory();
    /*
     * Private constructor to prevent instantiation of this utility class.
     */
    private InputSanitizer() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Sanitizes the input string by applying the defined HTML policy.
     * If the input is null, it returns an empty string.
     *
     * @param input The input string to sanitize.
     * @return The sanitized string, or an empty string if the input is null.
     */

    public static String sanitize(String input) {
        return input == null ? "" : POLICY.sanitize(input);
    }
}

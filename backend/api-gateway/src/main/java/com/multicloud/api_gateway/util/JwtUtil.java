package com.multicloud.api_gateway.util;

import io.jsonwebtoken.Jwts;  // For creating and parsing JWTs
import io.jsonwebtoken.io.Decoders;  // For decoding Base64 strings
import io.jsonwebtoken.security.Keys;  // For generating signing keys
import org.springframework.beans.factory.annotation.Value;  // For accessing configuration properties
import org.springframework.stereotype.Component;  // Indicates that this class is a Spring component

import java.security.Key;  // For using Java security keys

@Component  // Marks this class as a Spring-managed bean
public class JwtUtil {

    // Secret key used for signing and verifying JWTs, injected from application properties
    @Value("${security.jwt.secret-key}")
    private String secret;

    // Validates the provided JWT token by parsing it and checking its signature
    public void validateToken(final String token) {
        Jwts.parser()  // Create a JWT parser
                .setSigningKey(getSignKey())  // Set the signing key for validation
                .build()  // Build the parser
                .parseClaimsJws(token);  // Parse the token and validate its claims
    }

    // Retrieves the signing key for JWT validation
    private Key getSignKey() {
        // Decode the Base64 encoded secret key into bytes and create a signing key
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);  // Generate a HMAC signing key
    }
}

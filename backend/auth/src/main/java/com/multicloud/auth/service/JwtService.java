package com.multicloud.auth.service;

import com.multicloud.auth.model.User;
import io.jsonwebtoken.Claims;  // Represents the claims contained in a JWT
import io.jsonwebtoken.Jwts;  // Main class for creating and parsing JWTs
import io.jsonwebtoken.io.Decoders;  // Utility for decoding base64
import io.jsonwebtoken.security.Keys;  // Utility for generating cryptographic keys
import org.springframework.beans.factory.annotation.Value;  // For injecting configuration values
import org.springframework.security.core.userdetails.UserDetails;  // Represents user details
import org.springframework.stereotype.Service;  // Indicates that this class is a service component

import javax.crypto.SecretKey;
import java.util.Date;  // Represents date and time
import java.util.HashMap;  // HashMap for storing claims
import java.util.Map;  // General map interface
import java.util.function.Function;  // Functional interface for claims resolver

@Service  // Marks this class as a Spring service
public class JwtService {
    @Value("${security.jwt.secret-key}")  // Injects secret key from configuration
    private String secretKey;

    @Value("${security.jwt.expiration-time}")  // Injects expiration time from configuration
    private long jwtExpiration;

    // Extracts the username from the JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts a specific claim from the JWT token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Generates a JWT token based on user details
    public String generateToken(UserDetails userDetails) {
        Map<String,Object> extraClaims = new HashMap<>();
        if(userDetails instanceof User user){
            extraClaims.put("emailId",user.getEmail());
            extraClaims.put("userId",user.getId());
        }
        return generateToken(extraClaims, userDetails);
    }

    // Generates a JWT token with extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    // Returns the expiration time of the token
    public long getExpirationTime() {
        return jwtExpiration;
    }

    // Builds a JWT token with the specified claims and expiration time
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)  // Sets custom claims
                .subject(userDetails.getUsername()) // Sets the subject (username)
                .issuedAt(new Date(System.currentTimeMillis()))  // Sets the issued time
                .expiration(new Date(System.currentTimeMillis() + expiration))  // Sets the expiration time
                .signWith(getSignInKey(), Jwts.SIG.HS256)  // Signs the token
                .compact();  // Builds the token
    }

    // Validates the token against the user details
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);  // Extracts username from the token
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);  // Checks validity
    }

    // Checks if the token has expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());  // Compares expiration date with current date
    }

    // Extracts the expiration date from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extracts all claims from the JWT token
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())  // Sets the signing key
                .build()
                .parseSignedClaims(token)  // Parses the token
                .getPayload();  // Retrieves the claims
    }

    // Retrieves the signing key used for signing the token
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

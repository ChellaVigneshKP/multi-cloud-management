package com.multicloud.auth.service;

import com.multicloud.auth.exception.JweDecryptionException;
import com.multicloud.auth.exception.JweEncryptionException;
import com.multicloud.auth.exception.PrivateKeyLoadingException;
import com.multicloud.auth.exception.PublicKeyLoadingException;
import com.multicloud.auth.model.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

@Service
public class JweService {
    private static final Logger logger = LoggerFactory.getLogger(JweService.class);

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value("${security.jwt.public-key-path}")
    private String publicKeyPath;

    @Value("${security.jwt.private-key-path}")
    private String privateKeyPath;
    private final ResourceLoader resourceLoader;

    public JweService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    // Generate a JWE token
    public String generateToken(UserDetails userDetails) {
        Map<String,Object> extraClaims = new HashMap<>();
        if(userDetails instanceof User user){
            extraClaims.put("emailId",user.getEmail());
            extraClaims.put("userId",user.getId());
        }
        logger.debug("Extra Claims added for User");
        return generateToken(extraClaims, userDetails);
    }

    // Build the JWE token with extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        logger.debug("New Token Generated");
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    // Helper method to build the JWE token
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        try {
            // Create JWT Claims
            JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expiration));
            extraClaims.forEach(claimsSet::claim);
            JWTClaimsSet jwtClaims = claimsSet.build();

            // Load the public key
            RSAPublicKey publicKey = loadPublicKey();

            // Prepare JWE header
            JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM);

            // Create JWE object
            JWEObject jweObject = new JWEObject(header, new Payload(jwtClaims.toJSONObject()));

            // Encrypt with public key
            RSAEncrypter encrypter = new RSAEncrypter(publicKey);
            jweObject.encrypt(encrypter);

            // Serialize to compact form
            logger.debug("JWE Token Generated");
            return jweObject.serialize();

        } catch (JOSEException e) {
            throw new JweEncryptionException("Error while building JWE token", e);
        } catch (PublicKeyLoadingException e) {
            throw new JweEncryptionException("Error loading public key", e);
        }
    }

    // Extract the username from a JWE token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract all claims from the JWE token
    private JWTClaimsSet extractAllClaims(String token) {
        try {
            // Parse the JWE token
            JWEObject jweObject = JWEObject.parse(token);
            // Load the private key
            RSAPrivateKey privateKey = loadPrivateKey();
            // Decrypt with private key
            RSADecrypter decrypter = new RSADecrypter(privateKey);
            jweObject.decrypt(decrypter);
            String payload = jweObject.getPayload().toString();
            JWTClaimsSet claimsSet = JWTClaimsSet.parse(payload);
            // Extract claims
            logger.debug("Claims Extracted");
            return JWTClaimsSet.parse(jweObject.getPayload().toJSONObject());
        } catch (ParseException | JOSEException e) {
            throw new JweDecryptionException("Error while extracting claims from JWE token", e);
        } catch (PrivateKeyLoadingException e) {
            throw new JweDecryptionException("Error loading private key in extracting claims", e);
        }
    }

    // Validate the JWE token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Check if the token is expired
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpirationTime().before(new Date());
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    // Method to load the RSA public key from a file
    private RSAPublicKey loadPublicKey() throws PublicKeyLoadingException {
        try {
            Resource resource = resourceLoader.getResource(publicKeyPath);
            String publicKeyContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            publicKeyContent = publicKeyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            logger.debug("Public Key Loaded");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception e) {
            throw new PublicKeyLoadingException("Error loading public key", e);
        }
    }

    // Method to load the RSA private key from a file
    private RSAPrivateKey loadPrivateKey() throws PrivateKeyLoadingException {
        try {
            Resource resource = resourceLoader.getResource(privateKeyPath);
            String privateKeyContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            privateKeyContent = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            logger.debug("Private Key Loaded");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            logger.error("Error loading private key", e);
            throw new PrivateKeyLoadingException("Error loading private key", e);
        }
    }
}

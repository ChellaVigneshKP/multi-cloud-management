package com.multicloud.api_gateway.util;

import com.multicloud.api_gateway.exception.InvalidTokenException;
import com.multicloud.api_gateway.exception.PrivateKeyLoadingException;
import com.multicloud.api_gateway.exception.TokenExpiredException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

@Component
public class JweUtil {

    @Value("${security.jwt.private-key-path}")
    private String privateKeyPath;
    private final ResourceLoader resourceLoader;

    private RSAPrivateKey privateKey;

    public JweUtil(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void init() {
        this.privateKey = loadPrivateKey();
    }

    public JWTClaimsSet validateToken(String token) throws InvalidTokenException, TokenExpiredException {
        try {
            JWEObject jweObject = JWEObject.parse(token);
            RSADecrypter decrypter = new RSADecrypter(privateKey);
            jweObject.decrypt(decrypter);
            JWTClaimsSet claimsSet = JWTClaimsSet.parse(jweObject.getPayload().toJSONObject());

            // Validate token expiration
            if (claimsSet.getExpirationTime().before(new Date())) {
                throw new TokenExpiredException("Token has expired");
            }

            return claimsSet;
        } catch (ParseException | JOSEException e) {
            throw new InvalidTokenException("Invalid or malformed token", e);
        }
    }

    private RSAPrivateKey loadPrivateKey() {
        try {
            Resource resource = resourceLoader.getResource(privateKeyPath);
            InputStream is = resource.getInputStream();
            String privateKeyContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            privateKeyContent = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new PrivateKeyLoadingException("Error loading private key", e);
        }
    }
}

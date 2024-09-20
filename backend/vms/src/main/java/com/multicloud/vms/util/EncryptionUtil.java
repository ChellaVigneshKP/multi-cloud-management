package com.multicloud.vms.util;

import jakarta.annotation.PostConstruct;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    private BasicTextEncryptor textEncryptor;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public EncryptionUtil() {
        this.textEncryptor = new BasicTextEncryptor();
    }

    @PostConstruct
    private void init() {
        textEncryptor.setPassword(secretKey);
    }

    public String encrypt(String plainText) {
        return textEncryptor.encrypt(plainText);
    }

    public String decrypt(String encryptedText) {
        return textEncryptor.decrypt(encryptedText);
    }
}

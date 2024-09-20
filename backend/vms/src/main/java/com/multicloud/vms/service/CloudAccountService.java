package com.multicloud.vms.service;

import com.multicloud.vms.dto.CloudAccountDto;
import com.multicloud.vms.util.EncryptionUtil;
import com.multicloud.vms.model.CloudAccount;
import com.multicloud.vms.model.User;
import com.multicloud.vms.repository.CloudAccountRepository;
import com.multicloud.vms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CloudAccountService {

    @Autowired
    private CloudAccountRepository cloudAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionUtil encryptionUtil; // Inject EncryptionUtil

    public CloudAccount createGoogleCloudAccount(CloudAccountDto cloudAccountDto, MultipartFile googleCloudKey) {
        // Read the contents of the Google Cloud key file
        String credentials;
        try {
            credentials = new String(googleCloudKey.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Google Cloud key file", e);
        }

        // Set the credentials in the DTO
        cloudAccountDto.setCredentials(credentials);

        // Encrypt and save the credentials
        return encryptAndSaveCloudAccount(cloudAccountDto);
    }

    public CloudAccount createCloudAccount(CloudAccountDto cloudAccountDto) {
        // Encrypt and save the credentials
        return encryptAndSaveCloudAccount(cloudAccountDto);
    }

    private CloudAccount encryptAndSaveCloudAccount(CloudAccountDto cloudAccountDto) {
        CloudAccount cloudAccount = new CloudAccount();
        cloudAccount.setCloudProvider(cloudAccountDto.getCloudProvider());

        // Encrypt credentials
        String encryptedCredentials = encryptionUtil.encrypt(cloudAccountDto.getCredentials());
        cloudAccount.setCredentials(encryptedCredentials);

        cloudAccount.setRegion(cloudAccountDto.getRegion());

        // Associate the cloud account with a user
        User user = userRepository.findById(cloudAccountDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        cloudAccount.setUser(user);

        return cloudAccountRepository.save(cloudAccount);
    }

    public List<CloudAccount> getCloudAccountsByUser(Long userId) {
        return cloudAccountRepository.findByUserId(userId);
    }
}

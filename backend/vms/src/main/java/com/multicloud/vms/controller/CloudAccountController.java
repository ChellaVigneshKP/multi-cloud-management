package com.multicloud.vms.controller;

import com.multicloud.vms.dto.CloudAccountDto;
import com.multicloud.vms.model.CloudAccount;
import com.multicloud.vms.service.CloudAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile; // For file upload support

import java.util.List;

@RestController
@RequestMapping("/vm/cloud-accounts")
public class CloudAccountController {

    @Autowired
    private CloudAccountService cloudAccountService;

    // For Google Cloud (handles file upload)
    @PostMapping(value = "/google", consumes = {"multipart/form-data"})
    public ResponseEntity<CloudAccount> createGoogleCloudAccount(
            @RequestPart("cloudAccountDto") CloudAccountDto cloudAccountDto,
            @RequestPart("googleCloudKey") MultipartFile googleCloudKey) {

        CloudAccount cloudAccount = cloudAccountService.createGoogleCloudAccount(cloudAccountDto, googleCloudKey);
        return ResponseEntity.ok(cloudAccount);
    }

    // For AWS and Azure (regular request body)
    @PostMapping
    public ResponseEntity<CloudAccount> createCloudAccount(@RequestBody CloudAccountDto cloudAccountDto) {
        CloudAccount cloudAccount = cloudAccountService.createCloudAccount(cloudAccountDto);
        return ResponseEntity.ok(cloudAccount);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CloudAccount>> getCloudAccountsByUser(@PathVariable Long userId) {
        List<CloudAccount> cloudAccounts = cloudAccountService.getCloudAccountsByUser(userId);
        return ResponseEntity.ok(cloudAccounts);
    }
}


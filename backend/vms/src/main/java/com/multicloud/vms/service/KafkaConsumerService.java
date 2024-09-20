package com.multicloud.vms.service;

import com.multicloud.vms.model.User;
import com.multicloud.vms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaConsumerService {
    @Autowired
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "user-registration", groupId = "vm-service-group")
    public void consume(Map<String, String> userDetails) {
        String email = userDetails.get("email");
        String username = userDetails.get("username");
        if (email == null || username == null) {
            logger.error("Invalid user details received: {}", userDetails);
            return;
        }
        if (userRepository.findByEmail(email) != null) {
            logger.warn("User with email {} already exists.", email);
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        userRepository.save(user);
        logger.info("Received user registration event - Email: {}, Username: {}", email, username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

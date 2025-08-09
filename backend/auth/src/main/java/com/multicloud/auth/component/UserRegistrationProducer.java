package com.multicloud.auth.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserRegistrationProducer {
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationProducer.class);
    @Value("${app.messaging.kafka-user-registration-binding}")
    private String userRegistrationBinding;

    private final StreamBridge streamBridge;

    public UserRegistrationProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendUserRegisteredEvent(String username, Map<String, String> userDetails) {
        streamBridge.send(userRegistrationBinding, userDetails);
        logger.info("User registration event sent for username: {} to topic: {}", username, userRegistrationBinding);
    }
}
package com.multicloud.auth.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Map<String, String>> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Map<String, String>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserRegistrationEvent(Map<String, String> message) {
        kafkaTemplate.send("user-registration", message);
    }
}


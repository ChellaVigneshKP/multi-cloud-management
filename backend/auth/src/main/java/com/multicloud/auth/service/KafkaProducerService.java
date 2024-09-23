package com.multicloud.auth.service;

import org.springframework.kafka.core.KafkaTemplate;  // KafkaTemplate for sending messages to Kafka
import org.springframework.stereotype.Service;  // Indicates that this class is a service component

import java.util.Map;  // General map interface for message payloads

@Service  // Marks this class as a Spring service
public class KafkaProducerService {

    private final KafkaTemplate<String, Map<String, String>> kafkaTemplate;  // Kafka template for sending messages

    // Constructor to initialize KafkaTemplate
    public KafkaProducerService(KafkaTemplate<String, Map<String, String>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;  // Assigning the KafkaTemplate instance
    }

    // Sends a user registration event to the "user-registration" topic
    public void sendUserRegistrationEvent(Map<String, String> message) {
        kafkaTemplate.send("user-registration", message);  // Sending the message to the specified Kafka topic
    }
}

package com.multicloud.auth.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration  // Marks this class as a Spring configuration class
public class KafkaProducerConfig {

    @Bean  // Indicates that this method produces a bean to be managed by the Spring container
    public KafkaTemplate<String, Map<String, String>> kafkaTemplate() {
        // Create and return a KafkaTemplate for sending messages to Kafka
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean  // Indicates that this method produces a bean to be managed by the Spring container
    public ProducerFactory<String, Map<String, String>> producerFactory() {
        // Create a map to hold configuration properties for the Kafka producer
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9093");  // Set the Kafka broker address
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);  // Set the key serializer
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);  // Set the value serializer
        return new DefaultKafkaProducerFactory<>(configProps);  // Create and return the ProducerFactory
    }
}

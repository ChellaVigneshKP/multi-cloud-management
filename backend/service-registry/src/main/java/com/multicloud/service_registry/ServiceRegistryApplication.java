package com.multicloud.service_registry;

import org.springframework.boot.SpringApplication;  // For launching the Spring application
import org.springframework.boot.autoconfigure.SpringBootApplication;  // For enabling auto-configuration
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;  // For enabling Eureka Server functionality

@SpringBootApplication  // Marks this class as a Spring Boot application
@EnableEurekaServer  // Enables the Eureka server, allowing service registration and discovery
public class ServiceRegistryApplication {

	// Main method to run the Spring Boot application
	public static void main(String[] args) {
		// Launch the application
		SpringApplication.run(ServiceRegistryApplication.class, args);
	}

}

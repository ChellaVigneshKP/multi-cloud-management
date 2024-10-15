package com.multicloud.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private final DiscoveryClient discoveryClient;
    private final Logger logger = LoggerFactory.getLogger(OpenApiConfig.class);
    public OpenApiConfig(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        // Fetch API Gateway instances from the service registry (Eureka)
        List<String> apiGatewayInstances = discoveryClient.getInstances("api-gateway").stream()
                .map(serviceInstance -> serviceInstance.getUri().toString())  // Get the URI (IP and port)
                .toList();

        // Use the first available instance of the API Gateway
        apiGatewayInstances.forEach(instance -> logger.info("Available API Gateway Instance: {}", instance));
        String apiGatewayUrl = apiGatewayInstances.isEmpty() ? "http://localhost:6061" : apiGatewayInstances.getFirst();
        logger.info("API Gateway URL: {}", apiGatewayUrl);
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version("v1.0.0")
                        .description("Auth Service API for managing user authentication.")
                        .contact(new Contact()
                                .name("Chella Vignesh K P")
                                .url("https://chellavignesh.com")
                                .email("info@chellavignesh.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                .servers(List.of(
                        new Server()
                                .url(apiGatewayUrl)  // Dynamically set the API Gateway URL
                                .description("API Gateway Server")
                ));
    }
}

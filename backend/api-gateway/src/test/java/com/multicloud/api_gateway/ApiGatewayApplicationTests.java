package com.multicloud.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
		// This method is intentionally left empty as it serves as a basic
		// "sanity check" to verify that the Spring application context
		// loads successfully without throwing exceptions. This is often
		// used in Spring Boot projects as a quick way to confirm that the
		// basic configuration is valid. If the context fails to load,
		// this test will fail.
	}
}

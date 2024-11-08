package com.multicloud.api_gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AppConfigTests {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void restTemplateBeanIsNotNull() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void restTemplateBeanIsSingleton() {
        RestTemplate anotherRestTemplate = new AppConfig().template();
        assertThat(restTemplate).isSameAs(anotherRestTemplate);
    }
}

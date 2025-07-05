package com.multicloud.api_gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(AppConfig.class)
class AppConfigTests {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationContext context;

    @Test
    void restTemplateBeanIsNotNull() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void restTemplateBeanIsSingleton() {
        RestTemplate r1 = context.getBean(RestTemplate.class);
        RestTemplate r2 = context.getBean(RestTemplate.class);
        assertThat(r1).isSameAs(r2);
    }
}

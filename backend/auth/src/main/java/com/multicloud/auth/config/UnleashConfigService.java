package com.multicloud.auth.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnleashConfigService {

    @Value("${unleash.api.url}")
    private String unleashApiUrl;

    @Value("${unleash.api.key}")
    private String unleashApiKey;

    @Value("${unleash.app.name}")
    private String appName;

    @Value("${unleash.instance.id}")
    private String instanceId;

    @Bean
    public Unleash unleash() {
        UnleashConfig config = UnleashConfig.builder()
                .appName(appName)
                .instanceId(instanceId)
                .unleashAPI(unleashApiUrl)
                .apiKey(unleashApiKey)
                .build();
        return new DefaultUnleash(config);
    }
}

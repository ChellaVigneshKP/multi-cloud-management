package com.multicloud.auth.security.metrics.impl;

import com.multicloud.auth.security.metrics.SecurityMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrometheusSecurityMetricsService implements SecurityMetricsService {

    private static final String METRIC_PREFIX = "security.auth.";
    private final MeterRegistry registry;

    public PrometheusSecurityMetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void recordSuccessfulAuth(String username) {
        Counter.builder(METRIC_PREFIX + "success")
                .tags(List.of(
                        Tag.of("user", sanitize(username)),
                        Tag.of("auth_method", "jwt")
                ))
                .register(registry)
                .increment();
    }

    @Override
    public void recordFailedAuth(String identifier, String reason) {
        Counter.builder(METRIC_PREFIX + "failures")
                .tags(List.of(
                        Tag.of("identifier", sanitize(identifier)),
                        Tag.of("reason", reason),
                        Tag.of("source", "jwt_filter")
                ))
                .register(registry)
                .increment();
    }

    @Override
    public void recordSecurityIncident(String eventType) {
        Counter.builder(METRIC_PREFIX + "incidents")
                .tags("type", eventType)
                .register(registry)
                .increment();
    }

    private String sanitize(String input) {
        return input != null ? input.replaceAll("[^a-zA-Z0-9_\\-.]", "_") : "unknown";
    }
}
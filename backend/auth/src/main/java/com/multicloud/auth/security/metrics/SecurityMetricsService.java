package com.multicloud.auth.security.metrics;

public interface SecurityMetricsService {
    void recordSuccessfulAuth(String username);
    void recordFailedAuth(String identifier, String reason);
    void recordSecurityIncident(String eventType);
}

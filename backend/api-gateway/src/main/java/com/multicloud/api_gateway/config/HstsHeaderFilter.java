package com.multicloud.api_gateway.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static com.multicloud.commonlib.constants.DeviceConstants.HSTS_HEADER_VALUE;

@Component
public class HstsHeaderFilter implements WebFilter {

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.add("Strict-Transport-Security", HSTS_HEADER_VALUE);
        return chain.filter(exchange);
    }
}
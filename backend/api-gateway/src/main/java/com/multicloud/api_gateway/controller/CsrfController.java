package com.multicloud.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CsrfController {

    @GetMapping("/csrf")
    public Mono<ResponseEntity<Map<String, String>>> getCsrfToken(ServerWebExchange exchange) {
        Mono<CsrfToken> csrfTokenMono = exchange.getAttributeOrDefault(
                CsrfToken.class.getName(),
                Mono.empty()
        );
        return csrfTokenMono
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "CSRF token not available")))
                .map(token -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("token", token.getToken());
                    response.put("headerName", token.getHeaderName());
                    response.put("parameterName", token.getParameterName());
                    return ResponseEntity.ok(response);
                });
    }
}

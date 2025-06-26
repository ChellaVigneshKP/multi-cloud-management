package com.multicloud.adminserver.config;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "custom")
public class CustomEndpoint {

    @ReadOperation
    public String invoke() {
        return "Hello World!";
    }

}

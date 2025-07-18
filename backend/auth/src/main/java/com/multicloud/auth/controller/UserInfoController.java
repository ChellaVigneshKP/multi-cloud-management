package com.multicloud.auth.controller;

import com.multicloud.auth.responses.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/auth")
@RestController
@Tag(name = "UserInfo", description = "Endpoints to retrieve user information from request headers")
public class UserInfoController {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    @Operation(summary = "Get user info", description = "Retrieve user info from the request headers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: User information missing",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/userinfo")
    public ResponseEntity<Object> getUserInfo(
            @RequestHeader("X-User-Name") String username,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") String userId) {
        if (username == null || email == null || userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("User information is missing in the request headers"));
        }
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("email", email);
        userInfo.put("userId", userId);
        logger.info("UserInfo requested for User ID: {}", userId);
        return ResponseEntity.ok(userInfo);
    }
}

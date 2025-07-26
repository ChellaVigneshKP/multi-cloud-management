package com.multicloud.auth.exception;

import com.multicloud.auth.dto.responses.GeneralApiResponse;
import com.multicloud.commonlib.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<GeneralApiResponse<Void>> handleAccountLocked(AccountLockedException e) {
        return ResponseEntity.status(423).body(GeneralApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<GeneralApiResponse<Void>> handleNotVerified(AccountNotVerifiedException e) {
        return ResponseEntity.status(403).body(GeneralApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(TooManySessionsException.class)
    public ResponseEntity<GeneralApiResponse<Void>> handleTooManySessions(TooManySessionsException e) {
        return ResponseEntity.status(409).body(GeneralApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<GeneralApiResponse<Void>> handleUsernameNotFound(UsernameNotFoundException e) {
        return ResponseEntity.status(401).body(GeneralApiResponse.fail("Invalid credentials"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GeneralApiResponse<Void>> handleBadRequest(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(GeneralApiResponse.fail("Invalid request format"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GeneralApiResponse<Void>> handleGenericException(Exception e) {
        return ResponseEntity.status(500).body(GeneralApiResponse.error("Unexpected server error"));
    }

    @ExceptionHandler(TooManyDeviceAttemptsException.class)
    public ResponseEntity<GeneralApiResponse<Void>> handleTooManyDeviceAttempts(TooManyDeviceAttemptsException e) {
        return ResponseEntity.status(429).body(GeneralApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GeneralApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(401).body(GeneralApiResponse.fail("Authentication failed"));
    }

    @ExceptionHandler(EmailNotificationPublishException.class)
    public void handleEmailNotificationPublishException(EmailNotificationPublishException e) {
        log.error("Failed to send email notification: {}", e.getMessage());
    }
}

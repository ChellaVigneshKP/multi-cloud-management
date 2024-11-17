package com.multicloud.adminserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException ex) {
        logger.warn("Client disconnected during an SSE stream: {}", ex.getMessage());
    }
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException ex) {
        logger.warn("Connection aborted by client: {}", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public void handleIllegalStateException(IllegalStateException ex) {
        logger.warn("A non-container (application) thread attempted to use the AsyncContext: {}", ex.getMessage());
    }
}
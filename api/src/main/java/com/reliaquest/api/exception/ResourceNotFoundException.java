package com.reliaquest.api.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
        log.info("Resource not found: {}", message);
    }
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        log.info("Resource not found: {}", message);
    }
}
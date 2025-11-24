package com.configfy.configfyapi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Generic
    INTERNAL_SERVER_ERROR("GEN-001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("GEN-002", "Validation error", HttpStatus.BAD_REQUEST),

    // Authentication & Authorization
    INVALID_API_KEY("AUTH-100", "Invalid or expired API key", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("AUTH-101", "Unauthorized access", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("AUTH-102", "Forbidden", HttpStatus.FORBIDDEN),

    // Resources Not Found
    ENVIRONMENT_NOT_FOUND("NTF-200", "Environment not found", HttpStatus.NOT_FOUND),
    FLAG_NOT_FOUND("NTF-201", "Feature flag not found", HttpStatus.NOT_FOUND),
    VARIATION_NOT_FOUND("NTF-202", "Flag variation not found", HttpStatus.NOT_FOUND),
    RULE_NOT_FOUND("NTF-203", "Flag rule not found", HttpStatus.NOT_FOUND),

    // Business Logic
    ENVIRONMENT_PROTECTED("BUS-300", "Cannot modify protected environment", HttpStatus.FORBIDDEN),
    DUPLICATE_ENVIRONMENT("BUS-301", "Environment with this key already exists", HttpStatus.CONFLICT),
    DUPLICATE_FLAG("BUS-302", "Flag with this key already exists in this environment", HttpStatus.CONFLICT),
    INVALID_FLAG_TYPE("BUS-303", "Invalid flag type", HttpStatus.BAD_REQUEST),
    INVALID_OPERATOR("BUS-304", "Invalid rule operator", HttpStatus.BAD_REQUEST),

    // Rate Limiting
    RATE_LIMIT_EXCEEDED("LIM-400", "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

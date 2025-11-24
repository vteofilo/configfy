package com.configfy.configfyapi.exception;

import java.util.Map;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String resourceType, String identifier) {
        super(
                errorCode,
                String.format("%s with identifier '%s' not found", resourceType, identifier),
                Map.of("resourceType", resourceType, "identifier", identifier)
        );
    }
}

package com.configfy.configfyapi.exception;

import java.util.Map;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(ErrorCode errorCode, String resourceType, String identifier) {
        super(
                errorCode,
                String.format("%s with identifier '%s' already exists", resourceType, identifier),
                Map.of("resourceType", resourceType, "identifier", identifier)
        );
    }
}

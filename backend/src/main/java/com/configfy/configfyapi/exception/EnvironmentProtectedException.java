package com.configfy.configfyapi.exception;

import java.util.Map;

public class EnvironmentProtectedException extends BusinessException {

    public EnvironmentProtectedException(String environmentKey) {
        super(
                ErrorCode.ENVIRONMENT_PROTECTED,
                String.format("Cannot modify protected environment: %s", environmentKey),
                Map.of("environmentKey", environmentKey)
        );
    }
}

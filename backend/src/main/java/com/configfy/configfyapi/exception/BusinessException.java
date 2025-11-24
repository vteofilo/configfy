package com.configfy.configfyapi.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> metadata;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.metadata = null;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.metadata = null;
    }

    public BusinessException(ErrorCode errorCode, String customMessage, Map<String, Object> metadata) {
        super(customMessage);
        this.errorCode = errorCode;
        this.metadata = metadata;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.metadata = null;
    }
}

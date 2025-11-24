package com.configfy.configfyapi.exception;

public class InvalidApiKeyException extends BusinessException {

    public InvalidApiKeyException() {
        super(ErrorCode.INVALID_API_KEY);
    }

    public InvalidApiKeyException(String message) {
        super(ErrorCode.INVALID_API_KEY, message);
    }
}

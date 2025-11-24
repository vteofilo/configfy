package com.configfy.configfyapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private String method;
    private List<ValidationError> validationErrors;
    private Map<String, Object> metadata;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    public static ErrorResponse of(ErrorCode errorCode, String path, String method) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .method(method)
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String customMessage, String path, String method) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .timestamp(LocalDateTime.now())
                .path(path)
                .method(method)
                .build();
    }
}

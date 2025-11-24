package com.configfy.configfyapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBusinessException(
            BusinessException ex,
            ServerWebExchange exchange
    ) {
        log.warn("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .method(exchange.getRequest().getMethod().name())
                .metadata(ex.getMetadata())
                .build();

        return Mono.just(ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        log.warn("Validation error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .method(exchange.getRequest().getMethod().name())
                .validationErrors(validationErrors)
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange
    ) {
        log.error("Unexpected error", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .method(exchange.getRequest().getMethod().name())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerWebExchange exchange
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(exchange.getRequest().getPath().value())
                .method(exchange.getRequest().getMethod().name())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }
}

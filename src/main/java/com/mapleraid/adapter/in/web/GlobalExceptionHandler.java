package com.mapleraid.adapter.in.web;

import com.mapleraid.adapter.in.web.dto.ApiResponse;
import com.mapleraid.domain.common.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @SuppressWarnings("unchecked")
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        log.warn("Domain exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());

        Map<String, Object> details = null;
        if (ex.getDetails() instanceof Map) {
            details = (Map<String, Object>) ex.getDetails();
        } else if (ex.getDetails() != null) {
            details = Map.of("details", ex.getDetails());
        }

        if (details != null) {
            return ResponseEntity
                    .status(status)
                    .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), details));
        }

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "입력값이 올바르지 않습니다.", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
    }

    private HttpStatus mapErrorCodeToStatus(String errorCode) {
        if (errorCode == null) return HttpStatus.INTERNAL_SERVER_ERROR;

        if (errorCode.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        if (errorCode.contains("DUPLICATE") || errorCode.contains("ALREADY")) {
            return HttpStatus.CONFLICT;
        }
        if (errorCode.contains("NOT_OWNER") || errorCode.contains("NOT_AUTHOR")
                || errorCode.contains("NOT_MEMBER") || errorCode.contains("FORBIDDEN")) {
            return HttpStatus.FORBIDDEN;
        }
        if (errorCode.contains("INVALID_CREDENTIALS") || errorCode.contains("UNAUTHORIZED")) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (errorCode.contains("LIMIT") || errorCode.contains("COOLDOWN")
                || errorCode.contains("RATE_LIMIT")) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }

        return HttpStatus.BAD_REQUEST;
    }
}

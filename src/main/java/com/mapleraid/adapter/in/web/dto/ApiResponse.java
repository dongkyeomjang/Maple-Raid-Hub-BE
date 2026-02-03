package com.mapleraid.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorBody error,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, Map<String, Object> details) {
        return new ApiResponse<>(false, null, new ErrorBody(errorCode, message, details), Instant.now());
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return error(errorCode, message, null);
    }

    public record ErrorBody(
            String errorCode,
            String message,
            Map<String, Object> details
    ) {
    }
}

package com.mapleraid.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapleraid.core.exception.definition.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public sealed class ExceptionDto permits ArgumentNotValidExceptionDto {

    @JsonProperty("code")
    private final Integer code;

    @JsonProperty("message")
    private final String message;

    @Builder
    public ExceptionDto(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ExceptionDto of(ErrorCode errorCode) {
        return new ExceptionDto(errorCode.getCode(), errorCode.getMessage());
    }

    public static ExceptionDto of(ErrorCode errorCode, String message) {
        String msg = errorCode.getMessage();
        if (message != null && !message.isBlank()) {
            msg = message;
        }
        return new ExceptionDto(errorCode.getCode(), msg);
    }
}

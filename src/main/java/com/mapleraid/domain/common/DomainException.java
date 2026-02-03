package com.mapleraid.domain.common;

/**
 * 도메인 규칙 위반 예외
 */
public class DomainException extends RuntimeException {
    private final String errorCode;
    private final Object details;

    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public DomainException(String errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}

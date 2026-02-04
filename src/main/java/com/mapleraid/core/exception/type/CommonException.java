package com.mapleraid.core.exception.type;

import com.mapleraid.core.exception.definition.ErrorCode;
import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    public CommonException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    public CommonException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + (detailMessage != null ? " - " + detailMessage : ""));
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    @Override
    public String getMessage() {
        if (detailMessage != null && !detailMessage.isBlank()) {
            return detailMessage;
        }
        return errorCode.getMessage();
    }
}

package com.ktb.howard.ktb_community_server.image.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class InvalidImageStatusException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public InvalidImageStatusException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

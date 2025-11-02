package com.ktb.howard.ktb_community_server.image.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class InvalidMimeTypeException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public InvalidMimeTypeException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

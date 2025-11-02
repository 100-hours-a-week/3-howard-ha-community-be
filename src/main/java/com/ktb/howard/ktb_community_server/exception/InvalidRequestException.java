package com.ktb.howard.ktb_community_server.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public InvalidRequestException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

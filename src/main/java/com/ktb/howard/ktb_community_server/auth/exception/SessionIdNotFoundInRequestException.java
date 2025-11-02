package com.ktb.howard.ktb_community_server.auth.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class SessionIdNotFoundInRequestException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public SessionIdNotFoundInRequestException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

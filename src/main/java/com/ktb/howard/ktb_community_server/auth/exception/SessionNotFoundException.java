package com.ktb.howard.ktb_community_server.auth.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class SessionNotFoundException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public SessionNotFoundException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

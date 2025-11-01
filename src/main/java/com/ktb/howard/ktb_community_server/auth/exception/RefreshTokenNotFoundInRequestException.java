package com.ktb.howard.ktb_community_server.auth.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class RefreshTokenNotFoundInRequestException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public RefreshTokenNotFoundInRequestException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

package com.ktb.howard.ktb_community_server.member.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class PasswordNotMatchedException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public PasswordNotMatchedException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

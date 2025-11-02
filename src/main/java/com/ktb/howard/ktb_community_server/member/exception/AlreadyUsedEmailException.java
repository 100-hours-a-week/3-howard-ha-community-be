package com.ktb.howard.ktb_community_server.member.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class AlreadyUsedEmailException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public AlreadyUsedEmailException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

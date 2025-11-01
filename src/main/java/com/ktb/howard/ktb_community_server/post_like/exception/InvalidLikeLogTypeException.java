package com.ktb.howard.ktb_community_server.post_like.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class InvalidLikeLogTypeException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public InvalidLikeLogTypeException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

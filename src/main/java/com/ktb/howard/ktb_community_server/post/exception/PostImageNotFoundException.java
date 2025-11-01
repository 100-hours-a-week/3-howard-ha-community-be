package com.ktb.howard.ktb_community_server.post.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class PostImageNotFoundException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public PostImageNotFoundException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

package com.ktb.howard.ktb_community_server.image.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class ImageNotFoundException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public ImageNotFoundException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

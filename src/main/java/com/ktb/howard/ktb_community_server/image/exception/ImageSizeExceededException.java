package com.ktb.howard.ktb_community_server.image.exception;

import com.ktb.howard.ktb_community_server.api.ImageErrorCode;
import lombok.Getter;

@Getter
public class ImageSizeExceededException extends RuntimeException {

    private final ImageErrorCode errorCode;

    public ImageSizeExceededException(ImageErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

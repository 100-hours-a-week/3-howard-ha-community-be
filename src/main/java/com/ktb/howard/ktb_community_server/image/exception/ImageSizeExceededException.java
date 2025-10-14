package com.ktb.howard.ktb_community_server.image.exception;

import lombok.Getter;

@Getter
public class ImageSizeExceededException extends RuntimeException {

    private final Long imageSize;

    private final String mimeType;

    public ImageSizeExceededException(String message, Long imageSize, String mimeType) {
        super(message);
        this.imageSize = imageSize;
        this.mimeType = mimeType;
    }

}

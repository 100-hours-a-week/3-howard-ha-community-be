package com.ktb.howard.ktb_community_server.image.exception;

import lombok.Getter;

@Getter
public class ImageNotFoundException extends RuntimeException {

    private final Long imageId;
    private Long referenceId;

    public ImageNotFoundException(String message, Long imageId) {
        super(message);
        this.imageId = imageId;
    }

    public ImageNotFoundException(String message, Long imageId, Long referenceId) {
        super(message);
        this.imageId = imageId;
        this.referenceId = referenceId;
    }

}

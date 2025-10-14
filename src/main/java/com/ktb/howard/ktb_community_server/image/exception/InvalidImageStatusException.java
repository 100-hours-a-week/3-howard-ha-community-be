package com.ktb.howard.ktb_community_server.image.exception;

import com.ktb.howard.ktb_community_server.image.domain.ImageStatus;
import lombok.Getter;

@Getter
public class InvalidImageStatusException extends RuntimeException {

    private final ImageStatus imageStatus;

    public InvalidImageStatusException(String message, ImageStatus imageStatus) {
        super(message);
        this.imageStatus = imageStatus;
    }

}

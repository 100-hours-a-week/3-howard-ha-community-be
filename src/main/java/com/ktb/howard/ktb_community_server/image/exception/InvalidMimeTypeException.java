package com.ktb.howard.ktb_community_server.image.exception;

import lombok.Getter;

@Getter
public class InvalidMimeTypeException extends RuntimeException {

    private final String mimeType;

    public InvalidMimeTypeException(String message, String mimeType) {
        super(message);
        this.mimeType = mimeType;
    }

}

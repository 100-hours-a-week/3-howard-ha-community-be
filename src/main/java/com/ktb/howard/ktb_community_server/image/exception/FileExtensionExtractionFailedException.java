package com.ktb.howard.ktb_community_server.image.exception;

import lombok.Getter;

@Getter
public class FileExtensionExtractionFailedException extends RuntimeException {

    private final String fileName;

    public FileExtensionExtractionFailedException(String message, String fileName) {
        super(message);
        this.fileName = fileName;
    }

}

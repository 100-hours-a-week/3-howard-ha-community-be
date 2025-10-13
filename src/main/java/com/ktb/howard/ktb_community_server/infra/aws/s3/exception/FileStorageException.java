package com.ktb.howard.ktb_community_server.infra.aws.s3.exception;

import lombok.Getter;

@Getter
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

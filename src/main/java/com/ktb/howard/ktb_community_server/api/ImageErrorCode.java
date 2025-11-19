package com.ktb.howard.ktb_community_server.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {

    IMAGE_NOT_FOUND(BAD_REQUEST, "IMAGE_ERROR_01", "이미지를 찾을 수 없습니다."),
    IMAGE_COUNT_EXCEEDED(BAD_REQUEST, "IMAGE_ERROR_02", "이미지는 최대 5장까지 업로드 가능합니다."),
    IMAGE_SIZE_EXCEEDED(BAD_REQUEST, "IMAGE_ERROR_03", "이미지는 개당 최대 1MB까지 업로드 가능합니다."),
    EXTENSION_EXTRACTION_FAILED(BAD_REQUEST, "IMAGE_ERROR_04", "파일 확장자 추출에 실패했습니다."),
    INVALID_MIME_TYPE(BAD_REQUEST, "IMAGE_ERROR_05", "유효하지 않은 MIME Type, 이미지 파일만 업로드 가능합니다."),
    INVALID_IMAGE_STATUS(BAD_REQUEST, "IMAGE_ERROR_06", "이미지 상태가 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

}

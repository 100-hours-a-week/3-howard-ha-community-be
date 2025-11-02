package com.ktb.howard.ktb_community_server.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    POST_NOT_FOUND(BAD_REQUEST, "POST_ERROR_01", "게시글이 존재하지 않습니다."),
    POST_IMAGE_NOT_FOUND(BAD_REQUEST, "POST_ERROR_02", "게시글이 존재하지 않는 이미지를 포함하고 있습니다.");

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

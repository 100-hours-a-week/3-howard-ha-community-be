package com.ktb.howard.ktb_community_server.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum LikeLogErrorCode implements BaseErrorCode {

    INVALID_LIKE_LOG_TYPE(BAD_REQUEST, "LIKE_LOG_ERROR_001", "유효하지 않은 좋아요 로그 타입입니다.");

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

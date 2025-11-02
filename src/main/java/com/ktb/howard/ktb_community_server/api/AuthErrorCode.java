package com.ktb.howard.ktb_community_server.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    AUTH_ARGUMENT_NOT_FOUND(BAD_REQUEST, "AUTH_ERROR_001", "인증에 필요한 인자가 존재하지 않습니다."),
    INVALID_AUTH_RESPONSE_TYPE(BAD_REQUEST, "AUTH_ERROR_002", "유효하지 않은 인증 반환타입 입니다."),
    REFRESH_TOKEN_NOT_FOUND(BAD_REQUEST, "AUTH_ERROR_003", "Refresh Token 정보가 존재하지 않습니다."),
    SESSION_NOT_FOUND(BAD_REQUEST, "AUTH_ERROR_004", "Session 정보가 존재하지 않습니다."),
    SESSION_ID_NOT_FOUND_IN_REQUEST(BAD_REQUEST, "AUTH_ERROR_005", "요청에 Session ID가 없습니다."),
    REFRESH_TOKEN_NOT_FOUND_IN_REQUEST(BAD_REQUEST, "AUTH_ERROR_006", "요청에 Refresh Token이 없습니다.");

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

package com.ktb.howard.ktb_community_server.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    MEMBER_NOT_FOUND(BAD_REQUEST, "MEMBER_ERROR_001", "회원정보를 찾을 수 없습니다"),
    PASSWORD_NOT_MATCHED(BAD_REQUEST, "MEMBER_ERROR_002", "이메일 또는 비밀번호를 확인하세요");

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

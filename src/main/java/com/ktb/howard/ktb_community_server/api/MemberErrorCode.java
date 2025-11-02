package com.ktb.howard.ktb_community_server.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    MEMBER_NOT_FOUND(BAD_REQUEST, "MEMBER_ERROR_001", "회원정보를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCHED(BAD_REQUEST, "MEMBER_ERROR_002", "이메일 또는 비밀번호를 확인하세요."),
    PROFILE_IMAGE_NOT_FOUND_EXCEPTION(BAD_REQUEST, "MEMBER_ERROR_003", "지정한 프로필 이미지는 존재하지 않습니다."),
    ALREADY_USED_EMAIL(BAD_REQUEST, "MEMBER_ERROR_004", "이미 가입에 사용된 이메일 입니다."),
    ALREADY_USED_NICKNAME(BAD_REQUEST, "MEMBER_ERROR_005", "이미 가입에 사용된 닉네임 입니다.");

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

package com.ktb.howard.ktb_community_server.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum LambdaErrorCode implements BaseErrorCode {

    FUNCTION_CALL_FAILED(INTERNAL_SERVER_ERROR, "LAMBDA_ERROR_001", "Lambda 함수 실행에 실패하였습니다."),
    RESULT_SERIALIZE_ERROR(INTERNAL_SERVER_ERROR, "LAMBDA_ERROR_002", "Lambda 함수 실행 결과 직렬화에 실패하였습니다.");

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

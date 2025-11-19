package com.ktb.howard.ktb_community_server.infra.aws.lambda.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class LambdaFunctionCallFailedException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public LambdaFunctionCallFailedException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

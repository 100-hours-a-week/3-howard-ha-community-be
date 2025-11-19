package com.ktb.howard.ktb_community_server.infra.aws.lambda.exception;

import com.ktb.howard.ktb_community_server.api.BaseErrorCode;
import lombok.Getter;

@Getter
public class LambdaResultSerializeFailedException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public LambdaResultSerializeFailedException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}

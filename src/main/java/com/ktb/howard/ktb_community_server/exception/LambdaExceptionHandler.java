package com.ktb.howard.ktb_community_server.exception;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.infra.aws.lambda.exception.LambdaFunctionCallFailedException;
import com.ktb.howard.ktb_community_server.infra.aws.lambda.exception.LambdaResultSerializeFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LambdaExceptionHandler {

    @ExceptionHandler(LambdaFunctionCallFailedException.class)
    public ResponseEntity<ApiResponse<?>> handleLambdaFunctionCallFailedException(LambdaFunctionCallFailedException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(LambdaResultSerializeFailedException.class)
    public ResponseEntity<ApiResponse<?>> handleLambdaResultSerializeFailedException(LambdaResultSerializeFailedException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.internalServerError().body(response);
    }

}

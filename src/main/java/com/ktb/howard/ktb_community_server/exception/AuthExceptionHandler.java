package com.ktb.howard.ktb_community_server.exception;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.auth.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleSessionNotFoundException(
            SessionNotFoundException ex
    ) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SessionIdNotFoundInRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleSessionIdNotFoundException(
            SessionIdNotFoundInRequestException ex
    ) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RefreshTokenNotFoundInRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleRefreshTokenNotFoundInRequestException(
            RefreshTokenNotFoundInRequestException ex
    ) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidAuthResponseTypeException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidAuthResponseTypeException(
            InvalidAuthResponseTypeException ex
    ) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

}

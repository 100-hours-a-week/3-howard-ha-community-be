package com.ktb.howard.ktb_community_server.exception;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import com.ktb.howard.ktb_community_server.member.exception.PasswordNotMatchedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleMemberNotFoundException(MemberNotFoundException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PasswordNotMatchedException.class)
    public ResponseEntity<ApiResponse<?>> handlePasswordNotMatchedException(PasswordNotMatchedException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

}

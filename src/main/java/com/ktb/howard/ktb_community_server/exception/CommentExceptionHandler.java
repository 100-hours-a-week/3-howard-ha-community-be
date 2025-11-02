package com.ktb.howard.ktb_community_server.exception;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.comment.exception.CommentNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommentExceptionHandler {

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleCommentNotFoundException(CommentNotFoundException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

}

package com.ktb.howard.ktb_community_server.exception;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.image.exception.*;
import com.ktb.howard.ktb_community_server.member.exception.MemberNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ImageExceptionHandler {

    @ExceptionHandler(ExtensionExtractionFailedException.class)
    public ResponseEntity<ApiResponse<?>> handleExtensionExtractionFailedException(ExtensionExtractionFailedException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ImageCountExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleImageCountExceededException(ImageCountExceededException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleImageNotFoundException(ImageNotFoundException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ImageSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleImageSizeExceededException(ImageSizeExceededException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidImageStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidImageStatusException(InvalidImageStatusException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidMimeTypeException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidMimeTypeException(InvalidMimeTypeException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

}

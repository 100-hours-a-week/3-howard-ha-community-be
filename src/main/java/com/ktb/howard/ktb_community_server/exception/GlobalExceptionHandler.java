package com.ktb.howard.ktb_community_server.exception;

import com.ktb.howard.ktb_community_server.api.ApiResponse;
import com.ktb.howard.ktb_community_server.auth.exception.AuthArgumentNotFoundException;
import com.ktb.howard.ktb_community_server.auth.exception.RefreshTokenNotFoundException;
import com.ktb.howard.ktb_community_server.image.exception.*;
import com.ktb.howard.ktb_community_server.infra.aws.s3.exception.FileStorageException;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedEmailException;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedNicknameException;
import com.ktb.howard.ktb_community_server.post_like.exception.PostLikeAlreadyExistException;
import com.ktb.howard.ktb_community_server.post_like.exception.PostLikeNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ValidationErrorResponse.ValidationError> validationErrors = fieldErrors.stream()
                .map(error -> new ValidationErrorResponse.ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());
        return new ValidationErrorResponse("입력값이 올바르지 않습니다.", validationErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidationExceptions(ConstraintViolationException ex) {
        List<ValidationErrorResponse.ValidationError> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new ValidationErrorResponse.ValidationError(
                        java.util.stream.StreamSupport.stream(v.getPropertyPath().spliterator(), false)
                                .reduce((a, b) -> b).map(jakarta.validation.Path.Node::getName).orElse(""),
                        v.getMessage()
                ))
                .toList();
        return new ValidationErrorResponse("입력값이 올바르지 않습니다.", errors);
    }

    @ExceptionHandler(AlreadyUsedEmailException.class)
    public ResponseEntity<ApiResponse<?>> handleEmailConflictException(AlreadyUsedEmailException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(AlreadyUsedNicknameException.class)
    public ResponseEntity<ApiResponse<?>> handleNicknameConflictException(AlreadyUsedNicknameException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<String> handleFileStorageException(FileStorageException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExtensionExtractionFailedException.class)
    public ResponseEntity<String> handleFileExtensionExtractionException(ExtensionExtractionFailedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageSizeExceededException.class)
    public ResponseEntity<String> handleImageSizeExceededException(ImageSizeExceededException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageCountExceededException.class)
    public ResponseEntity<String> handleInvalidImageCountException(ImageCountExceededException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidImageStatusException.class)
    public ResponseEntity<String> handleInvalidImageStatusException(InvalidImageStatusException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidMimeTypeException.class)
    public ResponseEntity<String> handleInvalidMimeTypeException(InvalidMimeTypeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<String> handleImageNotFoundException(ImageNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PostLikeNotFoundException.class)
    public ResponseEntity<String> handlePostLikeNotFoundException(PostLikeNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PostLikeAlreadyExistException.class)
    public ResponseEntity<String> handlePostLikeAlreadyExistException(PostLikeAlreadyExistException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidRequestException(InvalidRequestException ex) {
        ApiResponse<?> response = ApiResponse.onFailure(ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<String> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthArgumentNotFoundException.class)
    public ResponseEntity<String> handleAuthArgumentNotFoundException(AuthArgumentNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
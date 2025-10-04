package com.ktb.howard.ktb_community_server.exception;

import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedEmailException;
import com.ktb.howard.ktb_community_server.member.exception.AlreadyUsedNicknameException;
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
    public ResponseEntity<String> handleEmailConflictException(AlreadyUsedEmailException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AlreadyUsedNicknameException.class)
    public ResponseEntity<String> handleNicknameConflictException(AlreadyUsedNicknameException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

}
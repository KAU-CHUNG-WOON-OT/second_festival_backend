package com.kau.chungwoon.festivalbackend.exception;

import com.kau.chungwoon.festivalbackend.response.ApiResponse;
import com.kau.chungwoon.festivalbackend.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        ErrorResponse.of(errorCode.getCode(), errorCode.getMessage())
                ));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
        String message;
        List<ErrorResponse.FieldErrorDetail> fieldErrors;

        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            fieldErrors = methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                    .map(error -> new ErrorResponse.FieldErrorDetail(
                            error.getField(),
                            error.getRejectedValue(),
                            error.getDefaultMessage()
                    ))
                    .toList();
            message = fieldErrors.stream()
                    .map(error -> error.field() + ": " + error.reason())
                    .collect(Collectors.joining(", "));
        } else if (exception instanceof BindException bindException) {
            fieldErrors = bindException.getBindingResult().getFieldErrors().stream()
                    .map(error -> new ErrorResponse.FieldErrorDetail(
                            error.getField(),
                            error.getRejectedValue(),
                            error.getDefaultMessage()
                    ))
                    .toList();
            message = fieldErrors.stream()
                    .map(error -> error.field() + ": " + error.reason())
                    .collect(Collectors.joining(", "));
        } else {
            message = exception.getMessage();
            fieldErrors = List.of();
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(
                        ErrorCode.INVALID_INPUT_VALUE.getCode(),
                        message,
                        ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE.getCode(), message, fieldErrors)
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        ErrorResponse.of(errorCode.getCode(), errorCode.getMessage())
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        ErrorResponse.of(errorCode.getCode(), errorCode.getMessage())
                ));
    }
}

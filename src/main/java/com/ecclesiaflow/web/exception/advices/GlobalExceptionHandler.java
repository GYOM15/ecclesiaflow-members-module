package com.ecclesiaflow.web.exception.advices;

import com.ecclesiaflow.web.exception.*;
import com.ecclesiaflow.web.exception.model.ApiErrorResponse;
import com.ecclesiaflow.web.exception.model.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            errors.add(new ValidationError(
                error.getDefaultMessage(),
                fieldName,
                "validation",
                error.getCode(),
                error.getDefaultMessage(),
                error.getCode(),
                null,
                null
            ));
        });

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Erreur de validation des données")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
            
        errorResponse.errors().addAll(errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(new ValidationError(
                violation.getMessage(),
                violation.getPropertyPath().toString(),
                "constraint",
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : "null",
                violation.getConstraintDescriptor().getAttributes().get("groups") != null ? 
                    violation.getConstraintDescriptor().getAttributes().get("groups").toString() : "CONSTRAINT_VIOLATION",
                null,
                null
            ));
        }

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Erreur de validation des contraintes")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
            
        errorResponse.errors().addAll(errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String error = "Requête JSON mal formée";
        
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(error)
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
            
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberNotFound(MemberNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidConfirmationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidConfirmationCode(InvalidConfirmationCodeException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ExpiredConfirmationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredConfirmationCode(ExpiredConfirmationCodeException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MemberAlreadyConfirmedException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberAlreadyConfirmed(MemberAlreadyConfirmedException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue", request);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message, WebRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
            
        return new ResponseEntity<>(errorResponse, status);
    }
}

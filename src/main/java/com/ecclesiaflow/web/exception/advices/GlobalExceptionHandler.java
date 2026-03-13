package com.ecclesiaflow.web.exception.advices;

import com.ecclesiaflow.business.exceptions.EmailAlreadyUsedException;
import com.ecclesiaflow.business.exceptions.EmailServiceUnavailableException;
import com.ecclesiaflow.business.exceptions.ExpiredConfirmationCodeException;
import com.ecclesiaflow.business.exceptions.InsufficientPermissionsException;
import com.ecclesiaflow.business.exceptions.InvalidConfirmationCodeException;

import com.ecclesiaflow.business.exceptions.InvalidEmailUpdateException;
import com.ecclesiaflow.business.exceptions.LocalCredentialsRequiredException;
import com.ecclesiaflow.business.exceptions.MemberAlreadyConfirmedException;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import com.ecclesiaflow.business.exceptions.SocialAccountAlreadyExistsException;
import com.ecclesiaflow.web.exception.*;
import com.ecclesiaflow.web.exception.model.ApiErrorResponse;
import com.ecclesiaflow.web.exception.model.ValidationError;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized exception handler that maps exceptions to standardized
 * {@link ApiErrorResponse} HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 400 Bad Request ---

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
            .message("Validation failed")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(errors)
            .build();

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
            .message("Constraint violation")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(errors)
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(
            "Malformed JSON request",
            "request",
            "parsing",
            "MalformedJson",
            "Malformed JSON request",
            "MalformedJson",
            null,
            null
        ));

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Malformed JSON request")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(errors)
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException ex, WebRequest request) {
        return buildBadRequestErrorResponse("Missing required header: " + ex.getHeaderName(), request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidConfirmationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidConfirmationCode(InvalidConfirmationCodeException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(ExpiredConfirmationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredConfirmationCode(ExpiredConfirmationCodeException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidEmailUpdateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidEmailUpdate(InvalidEmailUpdateException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    // --- 403 Forbidden ---

    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientPermissions(InsufficientPermissionsException ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(LocalCredentialsRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleLocalCredentialsRequired(LocalCredentialsRequiredException ex, WebRequest request) {
        String providerName = switch (ex.getProvider()) {
            case GOOGLE -> "Google";
            case MICROSOFT -> "Microsoft";
            case FACEBOOK -> "Facebook";
        };
        String message = "Your account was created with " + providerName
                + ". Please add a password before changing your email.";
        return buildSimpleErrorResponse(HttpStatus.FORBIDDEN, message, request);
    }

    // --- 404 Not Found ---

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberNotFound(MemberNotFoundException ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // --- 409 Conflict ---

    @ExceptionHandler(MemberAlreadyConfirmedException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberAlreadyConfirmed(MemberAlreadyConfirmedException ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyUsed(EmailAlreadyUsedException ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(SocialAccountAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleSocialAccountAlreadyExists(SocialAccountAlreadyExistsException ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // --- 429 / 503 ---

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(RequestNotPermitted ex, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(429)
            .error("Too Many Requests")
            .message("Too many requests. Please try again later.")
            .path(request.getRequestURI())
            .errors(null)
            .build();
        return ResponseEntity.status(429).body(errorResponse);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiErrorResponse> handleCircuitBreakerOpen(CallNotPermittedException ex, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
            .message("Service temporarily unavailable. Please try again later.")
            .path(request.getRequestURI())
            .errors(null)
            .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(EmailServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailServiceUnavailable(EmailServiceUnavailableException ex, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
            .message("Email service is temporarily unavailable. Your request has been noted and will be processed when the service recovers.")
            .path(request.getRequestURI())
            .errors(null)
            .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // --- 500 Catch-all ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred", request);
    }

    // --- Helpers ---

    /** Builds a 400 response with a single validation error entry. */
    private ResponseEntity<ApiErrorResponse> buildBadRequestErrorResponse(String message, WebRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(
            message, "request", "validation", "BadRequest",
            message, "BAD_REQUEST", null, null
        ));

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message(message)
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(errors)
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /** Builds an error response without validation details (errors = null). */
    private ResponseEntity<ApiErrorResponse> buildSimpleErrorResponse(HttpStatus status, String message, WebRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(null)
            .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}

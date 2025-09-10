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
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire global d'exceptions pour l'API REST EcclesiaFlow Members Module.
 * <p>
 * Cette classe centralise la gestion de toutes les exceptions levées par les contrôleurs
 * et les transforme en réponses HTTP standardisées avec le format {@link ApiErrorResponse}.
 * Utilise le pattern @RestControllerAdvice de Spring pour intercepter les exceptions
 * de manière transversale.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Aspect transversal - Gestion centralisée des erreurs</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Interception et transformation des exceptions métier en réponses HTTP</li>
 *   <li>Standardisation du format des erreurs avec {@link ApiErrorResponse}</li>
 *   <li>Gestion détaillée des erreurs de validation Bean Validation</li>
 *   <li>Mapping approprié des codes de statut HTTP selon le type d'erreur</li>
 *   <li>Logging et traçabilité des erreurs (implicite via Spring)</li>
 * </ul>
 * 
 * <p><strong>Types d'exceptions gérées :</strong></p>
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} - Erreurs de validation Bean Validation (400)</li>
 *   <li>{@link ConstraintViolationException} - Violations de contraintes (400)</li>
 *   <li>{@link HttpMessageNotReadableException} - JSON mal formé (400)</li>
 *   <li>{@link MemberNotFoundException} - Membre non trouvé (404)</li>
 *   <li>{@link InvalidConfirmationCodeException} - Code de confirmation invalide (400)</li>
 *   <li>{@link MemberAlreadyConfirmedException} - Membre déjà confirmé (409)</li>
 *   <li>{@link IllegalArgumentException} - Arguments invalides (400)</li>
 *   <li>{@link Exception} - Erreurs génériques (500)</li>
 * </ul>
 * 
 * <p><strong>Format de réponse standardisé :</strong></p>
 * <pre>{@code
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Erreur de validation des données",
 *   "path": "/ecclesiaflow/members",
 *   "errors": [
 *     {
 *       "message": "Le nom est obligatoire",
 *       "path": "firstName",
 *       "type": "validation",
 *       "code": "NotBlank"
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * <p><strong>Garanties :</strong> Gestion exhaustive, format cohérent, codes HTTP appropriés.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see ApiErrorResponse
 * @see ValidationError
 */
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
            .message("Erreur de validation des contraintes")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(errors)
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(
            "Requête JSON mal formée",
            "request",
            "parsing",
            "MalformedJson",
            "Requête JSON mal formée",
            "MalformedJson",
            null,
            null
        ));

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .message("Requête JSON mal formée")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(errors)
            .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException ex, WebRequest request) {
        String error = "En-tête requis manquant: " + ex.getHeaderName();
        return buildBadRequestErrorResponse(error, request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberNotFound(MemberNotFoundException ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidConfirmationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidConfirmationCode(InvalidConfirmationCodeException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(ExpiredConfirmationCodeException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredConfirmationCode(ExpiredConfirmationCodeException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(MemberAlreadyConfirmedException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberAlreadyConfirmed(MemberAlreadyConfirmedException ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return buildBadRequestErrorResponse(ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        return buildSimpleErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue", request);
    }

    /**
     * Construit une réponse d'erreur 400 Bad Request avec des erreurs de validation.
     * Utilisé pour les erreurs qui peuvent contenir des détails de validation.
     */
    private ResponseEntity<ApiErrorResponse> buildBadRequestErrorResponse(String message, WebRequest request) {
        // Pour les erreurs 400, on peut avoir des erreurs de validation
        // Si c'est une erreur simple, on crée une ValidationError générique
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(
            message,
            "request",
            "validation",
            "BadRequest",
            message,
            "BAD_REQUEST",
            null,
            null
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

    /**
     * Construit une réponse d'erreur simple sans erreurs de validation.
     * Utilisé pour les erreurs 404, 409, 500, etc. où le champ errors doit être null.
     */
    private ResponseEntity<ApiErrorResponse> buildSimpleErrorResponse(HttpStatus status, String message, WebRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(null)  // Explicitement null pour respecter le contrat OpenAPI
            .build();
            
        // Pour ces types d'erreurs (404, 409, 500), errors est null selon le contrat OpenAPI
        
        return new ResponseEntity<>(errorResponse, status);
    }
}

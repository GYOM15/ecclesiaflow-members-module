package com.ecclesiaflow.web.exception.advices;

import com.ecclesiaflow.web.exception.*;
import com.ecclesiaflow.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;

@ControllerAdvice
public class EmailExceptionHandler {

    @ExceptionHandler(ConfirmationEmailException.class)
    public ResponseEntity<ErrorResponse> handleConfirmationEmailException(ConfirmationEmailException ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'envoi de l'email de confirmation.", request.getRequestURI(), ex);
    }

    @ExceptionHandler(WelcomeEmailException.class)
    public ResponseEntity<ErrorResponse> handleWelcomeEmailException(WelcomeEmailException ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'envoi de l'email de bienvenue.", request.getRequestURI(), ex);
    }

    @ExceptionHandler(PasswordResetEmailException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetEmailException(PasswordResetEmailException ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'envoi de l'email de réinitialisation de mot de passe.", request.getRequestURI(), ex);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendingException(EmailSendingException ex, HttpServletRequest request) {
        // Catch-all pour toute autre exception d'email non spécifique
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'envoi de l'email.", request.getRequestURI(), ex);
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message, String path, Exception ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message + " Détails: " + ex.getMessage(),
                path
        );
        return new ResponseEntity<>(error, status);
    }
}

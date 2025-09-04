package com.ecclesiaflow.web.exception.advices;

import com.ecclesiaflow.io.email.EmailServiceImpl;
import com.ecclesiaflow.web.exception.*;
import com.ecclesiaflow.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;

/**
 * Gestionnaire d'exceptions spécialisé pour les erreurs liées à l'envoi d'emails.
 * <p>
 * Cette classe {@code @ControllerAdvice} capture et traite spécifiquement les exceptions
 * d'envoi d'email, fournissant des réponses d'erreur appropriées et un logging détaillé
 * pour le diagnostic des problèmes de messagerie.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Gestionnaire d'exceptions spécialisé - Emails</p>
 * 
 * <p><strong>Exceptions gérées :</strong></p>
 * <ul>
 *   <li>{@link ConfirmationEmailException} - Erreurs d'envoi de confirmation</li>
 *   <li>{@link WelcomeEmailException} - Erreurs d'envoi de bienvenue</li>
 *   <li>{@link PasswordResetEmailException} - Erreurs d'envoi de réinitialisation</li>
 *   <li>{@link EmailSendingException} - Erreurs génériques d'envoi</li>
 * </ul>
 * 
 * <p><strong>Stratégie de gestion :</strong></p>
 * <ul>
 *   <li>Toutes les erreurs d'email retournent HTTP 500 (Internal Server Error)</li>
 *   <li>Messages d'erreur spécifiques selon le type d'email</li>
 *   <li>Inclusion des détails techniques pour le debugging</li>
 *   <li>Logging automatique des exceptions pour monitoring</li>
 * </ul>
 * 
 * <p><strong>Sécurité :</strong> Les détails techniques sont inclus dans les réponses
 * pour faciliter le debugging, mais ne révèlent pas d'informations sensibles.</p>
 * 
 * <p><strong>Intégration :</strong> Fonctionne en complément de {@link GlobalExceptionHandler}
 * avec une priorité plus élevée pour les exceptions d'email spécifiques.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see GlobalExceptionHandler
 * @see EmailServiceImpl
 */
@ControllerAdvice
public class EmailExceptionHandler {

    /**
     * Gère les exceptions d'envoi d'email de confirmation d'inscription.
     * 
     * @param ex l'exception de confirmation email capturée
     * @param request la requête HTTP qui a causé l'exception
     * @return réponse d'erreur HTTP 500 avec détails spécifiques
     */
    @ExceptionHandler(ConfirmationEmailException.class)
    public ResponseEntity<ErrorResponse> handleConfirmationEmailException(ConfirmationEmailException ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'envoi de l'email de confirmation.", request.getRequestURI(), ex);
    }

    /**
     * Gère les exceptions d'envoi d'email de bienvenue.
     * 
     * @param ex l'exception de bienvenue email capturée
     * @param request la requête HTTP qui a causé l'exception
     * @return réponse d'erreur HTTP 500 avec détails spécifiques
     */
    @ExceptionHandler(WelcomeEmailException.class)
    public ResponseEntity<ErrorResponse> handleWelcomeEmailException(WelcomeEmailException ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'envoi de l'email de bienvenue.", request.getRequestURI(), ex);
    }

    /**
     * Gère les exceptions d'envoi d'email de réinitialisation de mot de passe.
     * 
     * @param ex l'exception de réinitialisation email capturée
     * @param request la requête HTTP qui a causé l'exception
     * @return réponse d'erreur HTTP 500 avec détails spécifiques
     */
    @ExceptionHandler(PasswordResetEmailException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetEmailException(PasswordResetEmailException ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'envoi de l'email de réinitialisation de mot de passe.", request.getRequestURI(), ex);
    }

    /**
     * Gère les exceptions génériques d'envoi d'email (catch-all).
     * 
     * @param ex l'exception d'envoi email générique capturée
     * @param request la requête HTTP qui a causé l'exception
     * @return réponse d'erreur HTTP 500 avec message générique
     */
    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendingException(EmailSendingException ex, HttpServletRequest request) {
        // Catch-all pour toute autre exception d'email non spécifique
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'envoi de l'email.", request.getRequestURI(), ex);
    }

    /**
     * Construit une réponse d'erreur standardisée pour les exceptions d'email.
     * 
     * @param status le statut HTTP à retourner
     * @param message le message d'erreur utilisateur
     * @param path le chemin de la requête qui a causé l'erreur
     * @param ex l'exception source pour les détails techniques
     * @return réponse d'erreur formatée avec {@link ErrorResponse}
     */
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

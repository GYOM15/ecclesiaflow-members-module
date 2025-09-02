package com.ecclesiaflow.web.exception.advices;

import com.ecclesiaflow.web.dto.ErrorResponse;
import com.ecclesiaflow.web.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour EmailExceptionHandler.
 * Vérifie la gestion spécialisée des exceptions d'envoi d'email et la standardisation des réponses d'erreur.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailExceptionHandler - Tests Unitaires")
class EmailExceptionHandlerTest {

    @InjectMocks
    private EmailExceptionHandler emailExceptionHandler;

    private MockHttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRequestURI("/ecclesiaflow/members");
    }

    // === TESTS POUR LES EXCEPTIONS D'EMAIL DE CONFIRMATION ===

    @Test
    @DisplayName("Devrait gérer ConfirmationEmailException avec statut 500")
    void handleConfirmationEmailException_ShouldReturnInternalServerError() {
        // Given
        String originalMessage = "Échec d'envoi du code de confirmation";
        RuntimeException cause = new RuntimeException("SMTP connection failed");
        ConfirmationEmailException exception = new ConfirmationEmailException(originalMessage, cause);

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleConfirmationEmailException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(500);
        assertThat(errorResponse.error()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.message()).isEqualTo("Erreur lors de l'envoi de l'email de confirmation. Détails: " + originalMessage);
        assertThat(errorResponse.path()).isEqualTo("/ecclesiaflow/members");
        assertThat(errorResponse.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Devrait gérer ConfirmationEmailException avec message null")
    void handleConfirmationEmailException_WithNullMessage_ShouldHandleGracefully() {
        // Given
        ConfirmationEmailException exception = new ConfirmationEmailException(null, new RuntimeException("Cause"));

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleConfirmationEmailException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Erreur lors de l'envoi de l'email de confirmation. Détails: null");
    }

    // === TESTS POUR LES EXCEPTIONS D'EMAIL DE BIENVENUE ===

    @Test
    @DisplayName("Devrait gérer WelcomeEmailException avec statut 500")
    void handleWelcomeEmailException_ShouldReturnInternalServerError() {
        // Given
        String originalMessage = "Template de bienvenue introuvable";
        RuntimeException cause = new RuntimeException("Template not found");
        WelcomeEmailException exception = new WelcomeEmailException(originalMessage, cause);

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleWelcomeEmailException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(500);
        assertThat(errorResponse.error()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.message()).isEqualTo("Erreur lors de l'envoi de l'email de bienvenue. Détails: " + originalMessage);
        assertThat(errorResponse.path()).isEqualTo("/ecclesiaflow/members");
        assertThat(errorResponse.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Devrait gérer WelcomeEmailException avec différents chemins de requête")
    void handleWelcomeEmailException_WithDifferentRequestPath_ShouldIncludeCorrectPath() {
        // Given
        httpServletRequest.setRequestURI("/ecclesiaflow/members/123/confirmation");
        WelcomeEmailException exception = new WelcomeEmailException("Test message", new RuntimeException());

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleWelcomeEmailException(exception, httpServletRequest);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path()).isEqualTo("/ecclesiaflow/members/123/confirmation");
    }

    // === TESTS POUR LES EXCEPTIONS D'EMAIL DE RÉINITIALISATION ===

    @Test
    @DisplayName("Devrait gérer PasswordResetEmailException avec statut 500")
    void handlePasswordResetEmailException_ShouldReturnInternalServerError() {
        // Given
        String originalMessage = "Token de réinitialisation expiré";
        RuntimeException cause = new RuntimeException("Token expired");
        PasswordResetEmailException exception = new PasswordResetEmailException(originalMessage, cause);

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handlePasswordResetEmailException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(500);
        assertThat(errorResponse.error()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.message()).isEqualTo("Erreur lors de l'envoi de l'email de réinitialisation de mot de passe. Détails: " + originalMessage);
        assertThat(errorResponse.path()).isEqualTo("/ecclesiaflow/members");
        assertThat(errorResponse.timestamp()).isNotNull();
    }

    // === TESTS POUR LES EXCEPTIONS GÉNÉRIQUES D'EMAIL ===

    @Test
    @DisplayName("Devrait gérer EmailSendingException avec statut 500")
    void handleEmailSendingException_ShouldReturnInternalServerError() {
        // Given
        String originalMessage = "Serveur SMTP indisponible";
        RuntimeException cause = new RuntimeException("SMTP server down");
        EmailSendingException exception = new EmailSendingException(originalMessage, cause);

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleEmailSendingException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(500);
        assertThat(errorResponse.error()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.message()).isEqualTo("Erreur lors de l'envoi de l'email. Détails: " + originalMessage);
        assertThat(errorResponse.path()).isEqualTo("/ecclesiaflow/members");
        assertThat(errorResponse.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Devrait gérer EmailSendingException avec message simple")
    void handleEmailSendingException_WithSimpleMessage_ShouldReturnCorrectResponse() {
        // Given
        String simpleMessage = "Configuration email invalide";
        EmailSendingException exception = new EmailSendingException(simpleMessage);

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleEmailSendingException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Erreur lors de l'envoi de l'email. Détails: " + simpleMessage);
    }

    // === TESTS POUR LA STRUCTURE DES RÉPONSES ===

    @Test
    @DisplayName("Devrait inclure un timestamp dans toutes les réponses d'erreur email")
    void allEmailErrorResponses_ShouldIncludeTimestamp() {
        // Given
        ConfirmationEmailException exception = new ConfirmationEmailException("Test", new RuntimeException());

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleConfirmationEmailException(exception, httpServletRequest);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Devrait retourner le même format de réponse pour toutes les exceptions email")
    void allEmailExceptions_ShouldReturnConsistentResponseFormat() {
        // Given
        ConfirmationEmailException confirmationException = new ConfirmationEmailException("Confirmation failed", new RuntimeException());
        WelcomeEmailException welcomeException = new WelcomeEmailException("Welcome failed", new RuntimeException());
        PasswordResetEmailException resetException = new PasswordResetEmailException("Reset failed", new RuntimeException());
        EmailSendingException genericException = new EmailSendingException("Generic failed", new RuntimeException());

        // When
        ResponseEntity<ErrorResponse> confirmationResponse = emailExceptionHandler.handleConfirmationEmailException(confirmationException, httpServletRequest);
        ResponseEntity<ErrorResponse> welcomeResponse = emailExceptionHandler.handleWelcomeEmailException(welcomeException, httpServletRequest);
        ResponseEntity<ErrorResponse> resetResponse = emailExceptionHandler.handlePasswordResetEmailException(resetException, httpServletRequest);
        ResponseEntity<ErrorResponse> genericResponse = emailExceptionHandler.handleEmailSendingException(genericException, httpServletRequest);

        // Then - Tous doivent avoir le même statut HTTP
        assertThat(confirmationResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(welcomeResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resetResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(genericResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        // Tous doivent avoir la même structure de réponse
        assertThat(confirmationResponse.getBody()).isNotNull();
        assertThat(welcomeResponse.getBody()).isNotNull();
        assertThat(resetResponse.getBody()).isNotNull();
        assertThat(genericResponse.getBody()).isNotNull();

        // Tous doivent avoir les mêmes champs remplis
        assertThat(confirmationResponse.getBody().status()).isEqualTo(500);
        assertThat(welcomeResponse.getBody().status()).isEqualTo(500);
        assertThat(resetResponse.getBody().status()).isEqualTo(500);
        assertThat(genericResponse.getBody().status()).isEqualTo(500);

        assertThat(confirmationResponse.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(welcomeResponse.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(resetResponse.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(genericResponse.getBody().error()).isEqualTo("Internal Server Error");
    }

    // === TESTS POUR LES CAS LIMITES ===

    @Test
    @DisplayName("Devrait gérer les exceptions avec des messages très longs")
    void handleEmailExceptions_WithLongMessages_ShouldHandleGracefully() {
        // Given
        String longMessage = "A".repeat(1000); // Message de 1000 caractères
        ConfirmationEmailException exception = new ConfirmationEmailException(longMessage, new RuntimeException());

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleConfirmationEmailException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains(longMessage);
    }

    @Test
    @DisplayName("Devrait gérer les exceptions avec des caractères spéciaux")
    void handleEmailExceptions_WithSpecialCharacters_ShouldHandleGracefully() {
        // Given
        String messageWithSpecialChars = "Erreur avec caractères spéciaux: àéèùç@#$%^&*()[]{}|\\:;\"'<>,.?/~`";
        WelcomeEmailException exception = new WelcomeEmailException(messageWithSpecialChars, new RuntimeException());

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleWelcomeEmailException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains(messageWithSpecialChars);
    }

    @Test
    @DisplayName("Devrait gérer les requêtes avec URI null")
    void handleEmailExceptions_WithNullRequestURI_ShouldHandleGracefully() {
        // Given
        httpServletRequest.setRequestURI(null);
        EmailSendingException exception = new EmailSendingException("Test message");

        // When
        ResponseEntity<ErrorResponse> response = emailExceptionHandler
                .handleEmailSendingException(exception, httpServletRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path()).isNull();
    }
}

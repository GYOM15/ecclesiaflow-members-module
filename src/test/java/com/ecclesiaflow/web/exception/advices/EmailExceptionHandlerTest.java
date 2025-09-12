package com.ecclesiaflow.web.exception.advices;

import com.ecclesiaflow.io.exception.ConfirmationEmailException;
import com.ecclesiaflow.io.exception.EmailSendingException;
import com.ecclesiaflow.io.exception.WelcomeEmailException;
import com.ecclesiaflow.web.exception.model.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

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

    private static final String DEFAULT_URI = "/ecclesiaflow/members";
    private static final String ALTERNATIVE_URI = "/ecclesiaflow/members/123/confirmation";
    private static final int INTERNAL_SERVER_ERROR_CODE = 500;
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

    @BeforeEach
    void setUp() {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRequestURI(DEFAULT_URI);
    }

    // === Utilities Methods ===

    /**
     * Méthode utilitaire pour valider la structure standard d'une réponse d'erreur email.
     */
    private void assertStandardEmailErrorResponse(ResponseEntity<ApiErrorResponse> response, 
                                                String expectedMessagePrefix, 
                                                String originalMessage, 
                                                String expectedPath) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        
        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(INTERNAL_SERVER_ERROR_CODE);
        assertThat(errorResponse.error()).isEqualTo(INTERNAL_SERVER_ERROR_MESSAGE);
        assertThat(errorResponse.message()).isEqualTo(expectedMessagePrefix + originalMessage);
        assertThat(errorResponse.path()).isEqualTo(expectedPath);
        assertThat(errorResponse.timestamp()).isNotNull();
        assertThat(errorResponse.errors()).isNull(); // Pas d'erreurs de validation pour les erreurs d'email
    }

    /**
     * Fournit les données de test pour les exceptions d'email.
     */
    private static Stream<Arguments> emailExceptionProvider() {
        return Stream.of(
            Arguments.of(
                new ConfirmationEmailException("Échec d'envoi du code de confirmation", new RuntimeException("SMTP failed")),
                "Erreur lors de l'envoi de l'email de confirmation. Détails: ",
                "ConfirmationEmailException"
            ),
            Arguments.of(
                new WelcomeEmailException("Template de bienvenue introuvable", new RuntimeException("Template not found")),
                "Erreur lors de l'envoi de l'email de bienvenue. Détails: ",
                "WelcomeEmailException"
            ),
            Arguments.of(
                new EmailSendingException("Serveur SMTP indisponible", new RuntimeException("SMTP server down")),
                "Erreur lors de l'envoi de l'email. Détails: ",
                "EmailSendingException"
            )
        );
    }

// === PARAMETERIZED TESTS FOR ALL EMAIL EXCEPTIONS ===

    @ParameterizedTest
    @MethodSource("emailExceptionProvider")
    @DisplayName("Devrait gérer toutes les exceptions email avec statut 500")
    void handleEmailExceptions_ShouldReturnInternalServerError(Exception exception, String expectedMessagePrefix, String exceptionType) {
        // When
        ResponseEntity<ApiErrorResponse> response = callAppropriateHandler(exception);

        // Then
        assertStandardEmailErrorResponse(response, expectedMessagePrefix, exception.getMessage(), DEFAULT_URI);
    }

    /**
     * Appelle le handler approprié selon le type d'exception.
     */
    private ResponseEntity<ApiErrorResponse> callAppropriateHandler(Exception exception) {
        if (exception instanceof ConfirmationEmailException) {
            return emailExceptionHandler.handleConfirmationEmailException((ConfirmationEmailException) exception, httpServletRequest);
        } else if (exception instanceof WelcomeEmailException) {
            return emailExceptionHandler.handleWelcomeEmailException((WelcomeEmailException) exception, httpServletRequest);
        } else if (exception instanceof EmailSendingException) {
            return emailExceptionHandler.handleEmailSendingException((EmailSendingException) exception, httpServletRequest);
        }
        throw new IllegalArgumentException("Type d'exception non supporté: " + exception.getClass());
    }

    // === SPÉCIFICS TESTS BY EXCEPTION TYPE ===

    @Nested
    @DisplayName("Tests pour ConfirmationEmailException")
    class ConfirmationEmailExceptionTests {

        @Test
        @DisplayName("Devrait gérer ConfirmationEmailException avec message null")
        void handleConfirmationEmailException_WithNullMessage_ShouldHandleGracefully() {
            // Given
            ConfirmationEmailException exception = new ConfirmationEmailException(null, new RuntimeException("Cause"));

            // When
            ResponseEntity<ApiErrorResponse> response = emailExceptionHandler
                    .handleConfirmationEmailException(exception, httpServletRequest);

            // Then
            assertStandardEmailErrorResponse(response, 
                "Erreur lors de l'envoi de l'email de confirmation. Détails: ", 
                "null", 
                DEFAULT_URI);
        }
    }

    @Nested
    @DisplayName("Tests pour WelcomeEmailException")
    class WelcomeEmailExceptionTests {

        @Test
        @DisplayName("Devrait gérer WelcomeEmailException avec différents chemins de requête")
        void handleWelcomeEmailException_WithDifferentRequestPath_ShouldIncludeCorrectPath() {
            // Given
            httpServletRequest.setRequestURI(ALTERNATIVE_URI);
            WelcomeEmailException exception = new WelcomeEmailException("Test message", new RuntimeException());

            // When
            ResponseEntity<ApiErrorResponse> response = emailExceptionHandler
                    .handleWelcomeEmailException(exception, httpServletRequest);

            // Then
            assertStandardEmailErrorResponse(response, 
                "Erreur lors de l'envoi de l'email de bienvenue. Détails: ", 
                "Test message", 
                ALTERNATIVE_URI);
        }
    }


    @Nested
    @DisplayName("Tests pour EmailSendingException")
    class EmailSendingExceptionTests {

        @Test
        @DisplayName("Devrait gérer EmailSendingException avec message simple")
        void handleEmailSendingException_WithSimpleMessage_ShouldReturnCorrectResponse() {
            // Given
            String simpleMessage = "Configuration email invalide";
            EmailSendingException exception = new EmailSendingException(simpleMessage);

            // When
            ResponseEntity<ApiErrorResponse> response = emailExceptionHandler
                    .handleEmailSendingException(exception, httpServletRequest);

            // Then
            assertStandardEmailErrorResponse(response, 
                "Erreur lors de l'envoi de l'email. Détails: ", 
                simpleMessage, 
                DEFAULT_URI);
        }
    }

    // === CONSISTENCY AND STRUCTURE TESTS ===

    @Nested
    @DisplayName("Tests de cohérence des réponses")
    class ResponseConsistencyTests {

        @Test
        @DisplayName("Toutes les exceptions email doivent retourner le même format de réponse")
        void allEmailExceptions_ShouldReturnConsistentResponseFormat() {
            // Given
            var exceptions = Stream.of(
                new ConfirmationEmailException("Confirmation failed", new RuntimeException()),
                new WelcomeEmailException("Welcome failed", new RuntimeException()),
                new EmailSendingException("Generic failed", new RuntimeException())
            );

            // When & Then
            exceptions.forEach(exception -> {
                ResponseEntity<ApiErrorResponse> response = callAppropriateHandler(exception);
                
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().status()).isEqualTo(INTERNAL_SERVER_ERROR_CODE);
                assertThat(response.getBody().error()).isEqualTo(INTERNAL_SERVER_ERROR_MESSAGE);
                assertThat(response.getBody().timestamp()).isNotNull();
                assertThat(response.getBody().path()).isEqualTo(DEFAULT_URI);
            });
        }
    }

    // === LIMIT CASES ===

    @Nested
    @DisplayName("Tests des cas limites")
    class EdgeCaseTests {

        /**
         * Fournit des données de test pour les cas limites.
         */
        private static Stream<Arguments> edgeCaseProvider() {
            return Stream.of(
                Arguments.of(
                    "Message très long",
                    "A".repeat(1000),
                    "Devrait gérer les messages très longs"
                ),
                Arguments.of(
                    "Caractères spéciaux",
                    "Erreur avec caractères spéciaux: àéèùç@#$%^&*()[]{}|\\:;\"'<>,.?/~`",
                    "Devrait gérer les caractères spéciaux"
                ),
                Arguments.of(
                    "Message vide",
                    "",
                    "Devrait gérer les messages vides"
                )
            );
        }

        @ParameterizedTest
        @MethodSource("edgeCaseProvider")
        @DisplayName("Devrait gérer gracieusement tous les cas limites de messages")
        void handleEmailExceptions_WithEdgeCases_ShouldHandleGracefully(String testCase, String message, String description) {
            // Given
            ConfirmationEmailException exception = new ConfirmationEmailException(message, new RuntimeException());

            // When
            ResponseEntity<ApiErrorResponse> response = emailExceptionHandler
                    .handleConfirmationEmailException(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains(message);
        }

        @Test
        @DisplayName("Devrait gérer les requêtes avec URI null")
        void handleEmailExceptions_WithNullRequestURI_ShouldHandleGracefully() {
            // Given
            httpServletRequest.setRequestURI(null);
            EmailSendingException exception = new EmailSendingException("Test message");

            // When
            ResponseEntity<ApiErrorResponse> response = emailExceptionHandler
                    .handleEmailSendingException(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().path()).isNull();
        }
    }
}

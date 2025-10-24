package com.ecclesiaflow.application.logging.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour SecurityContextLoggingAspect.
 * <p>
 * Teste chaque méthode d'advice individuellement avec des mocks
 * pour garantir une couverture de 100% du code.
 * </p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@DisplayName("SecurityContextLoggingAspect - Tests unitaires")
class SecurityContextLoggingAspectTest {

    private SecurityContextLoggingAspect aspect;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;
    private JoinPoint joinPoint;
    private Signature signature;

    @BeforeEach
    void setUp() {
        aspect = new SecurityContextLoggingAspect();

        // Configuration du logger pour capturer les logs
        logger = (Logger) LoggerFactory.getLogger(SecurityContextLoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);

        // Mock du JoinPoint
        joinPoint = mock(JoinPoint.class);
        signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
    }

    @AfterEach
    void tearDown() {
        if (logger != null) {
            logger.detachAppender(listAppender);
        }
        if (listAppender != null) {
            listAppender.stop();
        }
    }

    // === TESTS POUR L'EXTRACTION DU MEMBER ID ===

    @Test
    @DisplayName("logBeforeGetMemberId devrait logger la tentative d'extraction")
    void logBeforeGetMemberId_shouldLogDebug() {
        // When
        aspect.logBeforeGetMemberId(joinPoint);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(0).getFormattedMessage())
                .isEqualTo("Extracting authenticated member ID from JWT");
    }

    @Test
    @DisplayName("logAfterGetMemberId devrait logger le succès avec le memberId")
    void logAfterGetMemberId_shouldLogSuccessWithMemberId() {
        // Given
        UUID memberId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // When
        aspect.logAfterGetMemberId(memberId);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Successfully extracted member ID:")
                .contains("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    @DisplayName("logErrorGetMemberId devrait logger l'erreur pour claim 'cid' manquant")
    void logErrorGetMemberId_shouldLogErrorForMissingCidClaim() {
        // Given
        Exception exception = new IllegalStateException("Invalid JWT: missing 'cid' claim");

        // When
        aspect.logErrorGetMemberId(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .isEqualTo("JWT claim 'cid' (memberId) is missing");
    }

    @Test
    @DisplayName("logErrorGetMemberId devrait logger l'erreur pour format UUID invalide avec cause")
    void logErrorGetMemberId_shouldLogErrorForInvalidUuidFormatWithCause() {
        // Given
        IllegalArgumentException cause = new IllegalArgumentException("Invalid UUID string: invalid-uuid");
        Exception exception = new IllegalStateException("Invalid JWT: 'cid' is not a valid UUID", cause);

        // When
        aspect.logErrorGetMemberId(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Invalid JWT 'cid' format:")
                .contains("Invalid UUID string: invalid-uuid");
    }

    @Test
    @DisplayName("logErrorGetMemberId devrait logger l'erreur pour format UUID invalide sans cause")
    void logErrorGetMemberId_shouldLogErrorForInvalidUuidFormatWithoutCause() {
        // Given
        Exception exception = new IllegalStateException("Invalid JWT: 'cid' is not a valid UUID");

        // When
        aspect.logErrorGetMemberId(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Invalid JWT 'cid' format:")
                .contains("unknown");
    }

    @Test
    @DisplayName("logErrorGetMemberId devrait logger l'erreur générique")
    void logErrorGetMemberId_shouldLogGenericError() {
        // Given
        Exception exception = new IllegalStateException("Some other error");

        // When
        aspect.logErrorGetMemberId(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Failed to extract member ID from JWT:")
                .contains("Some other error");
    }

    @Test
    @DisplayName("logErrorGetMemberId devrait gérer une exception sans message")
    void logErrorGetMemberId_shouldHandleExceptionWithoutMessage() {
        // Given
        Exception exception = new IllegalStateException();

        // When
        aspect.logErrorGetMemberId(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
    }

    // === TESTS POUR L'EXTRACTION DES SCOPES ===

    @Test
    @DisplayName("logBeforeGetScopes devrait logger la tentative d'extraction")
    void logBeforeGetScopes_shouldLogDebug() {
        // When
        aspect.logBeforeGetScopes(joinPoint);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(0).getFormattedMessage())
                .isEqualTo("Extracting authenticated user scopes from JWT");
    }

    @Test
    @DisplayName("logAfterGetScopes devrait logger le succès avec les scopes")
    void logAfterGetScopes_shouldLogSuccessWithScopes() {
        // Given
        List<String> scopes = Arrays.asList("ef:members:read:own", "ef:members:write:own");

        // When
        aspect.logAfterGetScopes(scopes);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Successfully extracted 2 scopes:")
                .contains("ef:members:read:own")
                .contains("ef:members:write:own");
    }

    @Test
    @DisplayName("logAfterGetScopes devrait logger un warning pour liste vide")
    void logAfterGetScopes_shouldLogWarningForEmptyList() {
        // Given
        List<String> scopes = Collections.emptyList();

        // When
        aspect.logAfterGetScopes(scopes);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(logs.get(0).getFormattedMessage())
                .isEqualTo("JWT scope claim is missing or empty");
    }

    @Test
    @DisplayName("logAfterGetScopes devrait logger un warning pour liste null")
    void logAfterGetScopes_shouldLogWarningForNullList() {
        // When
        aspect.logAfterGetScopes(null);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(logs.get(0).getFormattedMessage())
                .isEqualTo("JWT scope claim is missing or empty");
    }

    @Test
    @DisplayName("logAfterGetScopes devrait logger le succès avec un seul scope")
    void logAfterGetScopes_shouldLogSuccessWithSingleScope() {
        // Given
        List<String> scopes = Collections.singletonList("ef:members:read:own");

        // When
        aspect.logAfterGetScopes(scopes);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Successfully extracted 1 scopes:")
                .contains("ef:members:read:own");
    }

    @Test
    @DisplayName("logErrorGetScopes devrait logger l'erreur")
    void logErrorGetScopes_shouldLogError() {
        // Given
        Exception exception = new IllegalStateException("Failed to parse JWT");

        // When
        aspect.logErrorGetScopes(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Failed to extract scopes from JWT:")
                .contains("Failed to parse JWT");
    }

    // === TESTS POUR LE PARSING JWT ===

    @Test
    @DisplayName("logErrorParseJwt devrait logger l'erreur de parsing")
    void logErrorParseJwt_shouldLogError() {
        // Given
        Exception exception = new IllegalStateException("Invalid JWT token");

        // When
        aspect.logErrorParseJwt(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Failed to parse JWT token:")
                .contains("Invalid JWT token");
    }

    @Test
    @DisplayName("logErrorParseJwt devrait logger l'erreur avec stacktrace")
    void logErrorParseJwt_shouldLogErrorWithStacktrace() {
        // Given
        Exception exception = new IllegalStateException("JWT signature validation failed");

        // When
        aspect.logErrorParseJwt(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getThrowableProxy()).isNotNull();
    }

    // === TESTS POUR L'EXTRACTION DU TOKEN ===

    @Test
    @DisplayName("logErrorExtractToken devrait logger l'erreur pour contexte manquant")
    void logErrorExtractToken_shouldLogErrorForMissingContext() {
        // Given
        Exception exception = new IllegalStateException("No HTTP request context available");

        // When
        aspect.logErrorExtractToken(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .isEqualTo("No request context available");
    }

    @Test
    @DisplayName("logErrorExtractToken devrait logger l'erreur pour header Authorization manquant")
    void logErrorExtractToken_shouldLogErrorForMissingAuthHeader() {
        // Given
        Exception exception = new IllegalStateException("Missing or invalid Authorization header");

        // When
        aspect.logErrorExtractToken(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .isEqualTo("Missing or invalid Authorization header");
    }

    @Test
    @DisplayName("logErrorExtractToken devrait logger l'erreur générique")
    void logErrorExtractToken_shouldLogGenericError() {
        // Given
        Exception exception = new IllegalStateException("Some other extraction error");

        // When
        aspect.logErrorExtractToken(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Failed to extract token from request:")
                .contains("Some other extraction error");
    }

    @Test
    @DisplayName("logErrorExtractToken devrait gérer une exception sans message")
    void logErrorExtractToken_shouldHandleExceptionWithoutMessage() {
        // Given
        Exception exception = new IllegalStateException();

        // When
        aspect.logErrorExtractToken(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
    }

    // === TESTS DE COUVERTURE SUPPLÉMENTAIRES ===

    @Test
    @DisplayName("Tous les advices devraient fonctionner avec des JoinPoints différents")
    void allAdvices_shouldWorkWithDifferentJoinPoints() {
        // Given
        JoinPoint anotherJoinPoint = mock(JoinPoint.class);
        Signature anotherSignature = mock(Signature.class);
        when(anotherJoinPoint.getSignature()).thenReturn(anotherSignature);
        when(anotherSignature.getName()).thenReturn("testMethod");

        // When - Test multiple advices
        aspect.logBeforeGetMemberId(anotherJoinPoint);
        aspect.logBeforeGetScopes(anotherJoinPoint);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(1).getLevel()).isEqualTo(Level.DEBUG);
    }

    @Test
    @DisplayName("logAfterGetMemberId devrait gérer différents UUIDs")
    void logAfterGetMemberId_shouldHandleDifferentUuids() {
        // Given
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        // When
        aspect.logAfterGetMemberId(uuid1);
        aspect.logAfterGetMemberId(uuid2);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getFormattedMessage()).contains(uuid1.toString());
        assertThat(logs.get(1).getFormattedMessage()).contains(uuid2.toString());
    }

    @Test
    @DisplayName("logAfterGetScopes devrait gérer différentes tailles de listes")
    void logAfterGetScopes_shouldHandleDifferentListSizes() {
        // Given
        List<String> smallList = Collections.singletonList("scope1");
        List<String> mediumList = Arrays.asList("scope1", "scope2", "scope3");
        List<String> largeList = Arrays.asList("s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10");

        // When
        aspect.logAfterGetScopes(smallList);
        aspect.logAfterGetScopes(mediumList);
        aspect.logAfterGetScopes(largeList);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(3);
        assertThat(logs.get(0).getFormattedMessage()).contains("1 scopes");
        assertThat(logs.get(1).getFormattedMessage()).contains("3 scopes");
        assertThat(logs.get(2).getFormattedMessage()).contains("10 scopes");
    }
}

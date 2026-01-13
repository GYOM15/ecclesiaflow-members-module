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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires directs pour BusinessOperationLoggingAspect.
 * Teste chaque méthode d'advice individuellement avec des mocks.
 */
@DisplayName("BusinessOperationLoggingAspect - Tests unitaires directs")
class BusinessOperationLoggingAspectTest {

    private BusinessOperationLoggingAspect aspect;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;
    private JoinPoint joinPoint;
    private Signature signature;

    @BeforeEach
    void setUp() {
        aspect = new BusinessOperationLoggingAspect();

        // Configuration du logger pour capturer les logs
        logger = (Logger) LoggerFactory.getLogger(BusinessOperationLoggingAspect.class);
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

    // === TESTS POUR L'ENREGISTREMENT DE MEMBRES ===

    @Test
    @DisplayName("logBeforeMemberRegistration devrait logger l'information")
    void logBeforeMemberRegistration_shouldLogInfo() {
        // When
        aspect.logBeforeMemberRegistration(joinPoint);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getLevel()).isEqualTo(Level.INFO);
        assertThat(logs.getFirst().getFormattedMessage())
                .contains("BUSINESS: Tentative d'enregistrement d'un nouveau membre");
    }

    @Test
    @DisplayName("logAfterSuccessfulRegistration devrait logger le succès")
    void logAfterSuccessfulRegistration_shouldLogInfo() {
        // When
        aspect.logAfterSuccessfulRegistration(joinPoint);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getLevel()).isEqualTo(Level.INFO);
        assertThat(logs.getFirst().getFormattedMessage())
                .contains("BUSINESS: Nouveau membre enregistré avec succès");
    }

    @Test
    @DisplayName("logFailedRegistration devrait logger l'échec")
    void logFailedRegistration_shouldLogWarning() {
        // Given
        RuntimeException exception = new RuntimeException("Erreur d'enregistrement");

        // When
        aspect.logFailedRegistration(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getLevel()).isEqualTo(Level.WARN);
        assertThat(logs.getFirst().getFormattedMessage())
                .contains("BUSINESS: Échec de l'enregistrement du membre")
                .contains("Erreur d'enregistrement");
    }

    // === TESTS POUR LES APPELS AU MODULE D'AUTHENTIFICATION ===

    @Test
    @DisplayName("logBeforeAuthModuleCall devrait logger avec le nom de méthode et arguments")
    void logBeforeAuthModuleCall_shouldLogDebug() {
        // Given
        when(signature.getName()).thenReturn("login");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"user", "password"});

        // When
        aspect.logBeforeAuthModuleCall(joinPoint);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.getFirst().getFormattedMessage())
                .contains("AUTH MODULE: Appel à login avec arguments: [user, password]");
    }

    @Test
    @DisplayName("logAfterSuccessfulAuthModuleCall devrait logger avec résultat non null")
    void logAfterSuccessfulAuthModuleCall_shouldLogWithResult() {
        // Given
        when(signature.getName()).thenReturn("getToken");
        String result = "token_123";

        // When
        aspect.logAfterSuccessfulAuthModuleCall(joinPoint, result);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.getFirst().getFormattedMessage())
                .contains("AUTH MODULE: getToken a réussi avec résultat: token_123");
    }

    @Test
    @DisplayName("logAfterSuccessfulAuthModuleCall devrait logger sans résultat si null")
    void logAfterSuccessfulAuthModuleCall_shouldLogWithoutResult() {
        // Given
        when(signature.getName()).thenReturn("logout");

        // When
        aspect.logAfterSuccessfulAuthModuleCall(joinPoint, null);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.getFirst().getFormattedMessage())
                .contains("AUTH MODULE: logout exécuté avec succès");
    }

    @Test
    @DisplayName("logAuthModuleError devrait logger l'erreur")
    void logAuthModuleError_shouldLogWarning() {
        // Given
        when(signature.getName()).thenReturn("refreshToken");
        RuntimeException exception = new RuntimeException("Token expiré");

        // When
        aspect.logAuthModuleError(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getLevel()).isEqualTo(Level.WARN);
        assertThat(logs.getFirst().getFormattedMessage())
                .contains("AUTH MODULE: Échec de l'appel à refreshToken")
                .contains("Token expiré");
    }

    @Test
    @DisplayName("logBeforeAuthModuleCall devrait gérer les arguments vides")
    void logBeforeAuthModuleCall_shouldHandleEmptyArgs() {
        // Given
        when(signature.getName()).thenReturn("logout");
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        // When
        aspect.logBeforeAuthModuleCall(joinPoint);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getFormattedMessage())
                .contains("AUTH MODULE: Appel à logout avec arguments: []");
    }
}

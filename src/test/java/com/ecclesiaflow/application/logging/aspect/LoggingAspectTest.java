package com.ecclesiaflow.application.logging.aspect;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ecclesiaflow.application.logging.annotation.LogExecution;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires directs pour LoggingAspect.
 * Teste chaque méthode d'advice individuellement avec des mocks.
 */
@DisplayName("LoggingAspect - Tests unitaires directs")
class LoggingAspectTest {

    private LoggingAspect aspect;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;
    private ProceedingJoinPoint proceedingJoinPoint;
    private JoinPoint joinPoint;
    private Signature signature;
    private Object target;

    @BeforeEach
    void setUp() {
        aspect = new LoggingAspect();

        // Configuration du logger pour capturer les logs
        logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);

        // Mock du ProceedingJoinPoint
        proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        signature = mock(Signature.class);
        target = new Object() {
            @Override
            public String toString() {
                return "TestTarget";
            }
        };

        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        when(proceedingJoinPoint.getTarget()).thenReturn(target);
        when(signature.getName()).thenReturn("testMethod");

        // Mock du JoinPoint
        joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
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

    // === TESTS POUR logServiceMethods ===

    @Test
    @DisplayName("logServiceMethods devrait logger l'exécution rapide")
    void logServiceMethods_shouldLogFastExecution() throws Throwable {
        // Given
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.logServiceMethods(proceedingJoinPoint);

        // Then
        assertThat(result).isEqualTo("result");
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSizeGreaterThanOrEqualTo(2);

        boolean hasStartLog = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("SERVICE: Début"));
        assertThat(hasStartLog).isTrue();

        boolean hasSuccessLog = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("SERVICE:")
                        && log.getFormattedMessage().contains("Succès"));
        assertThat(hasSuccessLog).isTrue();
    }

    @Test
    @DisplayName("logServiceMethods devrait logger les exécutions lentes (>1s)")
    void logServiceMethods_shouldLogSlowExecution() throws Throwable {
        // Given
        when(proceedingJoinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(1100); // > 1 seconde
            return "result";
        });

        // When
        Object result = aspect.logServiceMethods(proceedingJoinPoint);

        // Then
        assertThat(result).isEqualTo("result");
        List<ILoggingEvent> logs = listAppender.list;

        boolean hasSlowWarning = logs.stream()
                .anyMatch(log -> log.getLevel().equals(Level.WARN)
                        && log.getFormattedMessage().contains("Exécution lente"));
        assertThat(hasSlowWarning).isTrue();
    }

    @Test
    @DisplayName("logServiceMethods devrait logger les exceptions")
    void logServiceMethods_shouldLogException() throws Throwable {
        // Given
        Exception testException = new RuntimeException("Test error");
        when(proceedingJoinPoint.proceed()).thenThrow(testException);

        // When & Then
        assertThatThrownBy(() -> aspect.logServiceMethods(proceedingJoinPoint))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test error");

        List<ILoggingEvent> logs = listAppender.list;
        boolean hasErrorLog = logs.stream()
                .anyMatch(log -> log.getLevel().equals(Level.ERROR)
                        && log.getFormattedMessage().contains("Échec"));
        assertThat(hasErrorLog).isTrue();
    }

    // === TESTS POUR logAnnotatedMethods ===

    @Test
    @DisplayName("logAnnotatedMethods devrait utiliser le message personnalisé")
    void logAnnotatedMethods_shouldUseCustomMessage() throws Throwable {
        // Given
        LogExecution logExecution = mock(LogExecution.class);
        when(logExecution.value()).thenReturn("Custom operation");
        when(logExecution.includeParams()).thenReturn(false);
        when(logExecution.includeExecutionTime()).thenReturn(false);
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.logAnnotatedMethods(proceedingJoinPoint, logExecution);

        // Then
        assertThat(result).isEqualTo("result");
        List<ILoggingEvent> logs = listAppender.list;

        boolean hasCustomMessage = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("Custom operation"));
        assertThat(hasCustomMessage).isTrue();
    }

    @Test
    @DisplayName("logAnnotatedMethods devrait utiliser le nom par défaut si value est vide")
    void logAnnotatedMethods_shouldUseDefaultName() throws Throwable {
        // Given
        LogExecution logExecution = mock(LogExecution.class);
        when(logExecution.value()).thenReturn("");
        when(logExecution.includeParams()).thenReturn(false);
        when(logExecution.includeExecutionTime()).thenReturn(false);
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.logAnnotatedMethods(proceedingJoinPoint, logExecution);

        // Then
        assertThat(result).isEqualTo("result");
        List<ILoggingEvent> logs = listAppender.list;

        boolean hasDefaultName = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("testMethod"));
        assertThat(hasDefaultName).isTrue();
    }

    @Test
    @DisplayName("logAnnotatedMethods devrait logger les paramètres si includeParams est true")
    void logAnnotatedMethods_shouldLogParameters() throws Throwable {
        // Given
        LogExecution logExecution = mock(LogExecution.class);
        when(logExecution.value()).thenReturn("Operation");
        when(logExecution.includeParams()).thenReturn(true);
        when(logExecution.includeExecutionTime()).thenReturn(false);
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"param1", "param2"});
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.logAnnotatedMethods(proceedingJoinPoint, logExecution);

        // Then
        assertThat(result).isEqualTo("result");
        List<ILoggingEvent> logs = listAppender.list;

        boolean hasParams = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("Paramètres: [param1, param2]"));
        assertThat(hasParams).isTrue();
    }

    @Test
    @DisplayName("logAnnotatedMethods ne devrait pas logger les paramètres si includeParams est false")
    void logAnnotatedMethods_shouldNotLogParameters() throws Throwable {
        // Given
        LogExecution logExecution = mock(LogExecution.class);
        when(logExecution.value()).thenReturn("Operation");
        when(logExecution.includeParams()).thenReturn(false);
        when(logExecution.includeExecutionTime()).thenReturn(false);
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{"param1"});
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        aspect.logAnnotatedMethods(proceedingJoinPoint, logExecution);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        boolean hasParams = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("Paramètres:"));
        assertThat(hasParams).isFalse();
    }

    @Test
    @DisplayName("logAnnotatedMethods devrait logger le temps d'exécution si includeExecutionTime est true")
    void logAnnotatedMethods_shouldLogExecutionTime() throws Throwable {
        // Given
        LogExecution logExecution = mock(LogExecution.class);
        when(logExecution.value()).thenReturn("Operation");
        when(logExecution.includeParams()).thenReturn(false);
        when(logExecution.includeExecutionTime()).thenReturn(true);
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        Object result = aspect.logAnnotatedMethods(proceedingJoinPoint, logExecution);

        // Then
        assertThat(result).isEqualTo("result");
        List<ILoggingEvent> logs = listAppender.list;

        boolean hasExecutionTime = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().matches(".*Succès.*\\(\\d+ms\\).*"));
        assertThat(hasExecutionTime).isTrue();
    }

    @Test
    @DisplayName("logAnnotatedMethods ne devrait pas logger le temps si includeExecutionTime est false")
    void logAnnotatedMethods_shouldNotLogExecutionTime() throws Throwable {
        // Given
        LogExecution logExecution = mock(LogExecution.class);
        when(logExecution.value()).thenReturn("Operation");
        when(logExecution.includeParams()).thenReturn(false);
        when(logExecution.includeExecutionTime()).thenReturn(false);
        when(proceedingJoinPoint.proceed()).thenReturn("result");

        // When
        aspect.logAnnotatedMethods(proceedingJoinPoint, logExecution);

        // Then
        List<ILoggingEvent> logs = listAppender.list;

        // Le message de succès ne devrait pas contenir de temps
        boolean hasSuccessWithoutTime = logs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("Succès: Operation")
                        && !log.getFormattedMessage().matches(".*\\(\\d+ms\\).*"));
        assertThat(hasSuccessWithoutTime).isTrue();
    }

    @Test
    @DisplayName("logAnnotatedMethods devrait logger les exceptions avec le temps")
    void logAnnotatedMethods_shouldLogExceptionWithTime() throws Throwable {
        // Given
        LogExecution logExecution = mock(LogExecution.class);
        when(logExecution.value()).thenReturn("Operation");
        when(logExecution.includeParams()).thenReturn(false);
        when(logExecution.includeExecutionTime()).thenReturn(false);

        Exception testException = new RuntimeException("Test error");
        when(proceedingJoinPoint.proceed()).thenThrow(testException);

        // When & Then
        assertThatThrownBy(() -> aspect.logAnnotatedMethods(proceedingJoinPoint, logExecution))
                .isInstanceOf(RuntimeException.class);

        List<ILoggingEvent> logs = listAppender.list;
        boolean hasErrorWithTime = logs.stream()
                .anyMatch(log -> log.getLevel().equals(Level.ERROR)
                        && log.getFormattedMessage().contains("Échec")
                        && log.getFormattedMessage().matches(".*\\(\\d+ms\\).*"));
        assertThat(hasErrorWithTime).isTrue();
    }

    // === TESTS POUR logControllerAccess ===

    @Test
    @DisplayName("logControllerAccess devrait logger en DEBUG")
    void logControllerAccess_shouldLogDebug() {
        // When
        aspect.logControllerAccess(joinPoint);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("API:")
                .contains("testMethod");
    }

    // === TESTS POUR logUnhandledException ===

    @Test
    @DisplayName("logUnhandledException devrait logger l'exception")
    void logUnhandledException_shouldLogException() {
        // Given
        RuntimeException exception = new RuntimeException("Unhandled error");

        // When
        aspect.logUnhandledException(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("Exception non gérée")
                .contains("testMethod")
                .contains("RuntimeException")
                .contains("Unhandled error");
    }

    @Test
    @DisplayName("logUnhandledException devrait logger différents types d'exceptions")
    void logUnhandledException_shouldLogDifferentExceptionTypes() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        aspect.logUnhandledException(joinPoint, exception);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getFormattedMessage())
                .contains("IllegalArgumentException")
                .contains("Invalid argument");
    }
}

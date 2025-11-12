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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour {@link GrpcClientLoggingAspect}.
 * <p>
 * Vérifie le logging correct des appels gRPC sortants vers le module Auth,
 * incluant le shutdown, les RPC calls et la gestion des erreurs.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GrpcClientLoggingAspect - Tests du logging gRPC client")
class GrpcClientLoggingAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private GrpcClientLoggingAspect aspect;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(GrpcClientLoggingAspect.class);
        logger.setLevel(Level.DEBUG); // Capture DEBUG logs
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        listAppender.stop();
        logger.detachAppender(listAppender);
    }

    // ========================================================================
    // Tests - Shutdown du canal gRPC
    // ========================================================================

    @Test
    @DisplayName("Doit logger avant le shutdown du canal gRPC")
    void shouldLogBeforeChannelShutdown() {
        // When
        aspect.logBeforeChannelShutdown(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Initiating graceful shutdown");
    }

    @Test
    @DisplayName("Doit logger après un shutdown réussi")
    void shouldLogAfterSuccessfulShutdown() {
        // When
        aspect.logAfterChannelShutdown(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "closed successfully");
    }

    @Test
    @DisplayName("Doit logger les erreurs lors du shutdown")
    void shouldLogShutdownError() {
        // Given
        Exception exception = new RuntimeException("Connection timeout");

        // When
        aspect.logChannelShutdownError(joinPoint, exception);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.ERROR);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Error while closing", "RuntimeException");
    }

    // ========================================================================
    // Tests - Appels RPC (generateTemporaryToken)
    // ========================================================================

    @Test
    @DisplayName("Doit logger avant un appel generateTemporaryToken")
    void shouldLogBeforeGenerateTemporaryToken() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("generateTemporaryToken");

        // When
        aspect.logBeforeRpcCall(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Calling Auth.generateTemporaryToken");
    }

    @Test
    @DisplayName("Doit logger après un appel generateTemporaryToken réussi")
    void shouldLogAfterSuccessfulGenerateTemporaryToken() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("generateTemporaryToken");
        Object result = "token-value";

        // When
        aspect.logAfterSuccessfulRpcCall(joinPoint, result);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Auth.generateTemporaryToken completed successfully");
    }

    // ========================================================================
    // Tests - Appels RPC (autres méthodes en DEBUG)
    // ========================================================================

    @Test
    @DisplayName("Doit logger avant un appel RPC standard en DEBUG")
    void shouldLogBeforeStandardRpcCall() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("validateToken");

        // When
        aspect.logBeforeRpcCall(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.DEBUG);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Calling Auth.validateToken");
    }

    @Test
    @DisplayName("Doit logger après un appel RPC standard réussi en DEBUG")
    void shouldLogAfterSuccessfulStandardRpcCall() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("validateToken");
        Object result = Boolean.TRUE;

        // When
        aspect.logAfterSuccessfulRpcCall(joinPoint, result);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.DEBUG);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Auth.validateToken completed successfully");
    }

    // ========================================================================
    // Tests - Gestion des erreurs RPC
    // ========================================================================

    @Test
    @DisplayName("Doit logger une erreur UNAVAILABLE avec service indisponible")
    void shouldLogUnavailableServiceError() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("generateTemporaryToken");
        class ServiceUNAVAILABLEError extends RuntimeException {
            public ServiceUNAVAILABLEError(String message) {
                super(message);
            }
        }
        Exception exception = new ServiceUNAVAILABLEError("Auth service down");

        // When
        for (int i = 0; i < 50; i++) {
            aspect.logRpcCallError(joinPoint, exception);
        }

        // Then
        assertThat(listAppender.list)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "UNAVAILABLE", "generateTemporaryToken");
    }

    @Test
    @DisplayName("Doit logger une erreur Unavailable avec Unavailable dans le nom")
    void shouldLogUnavailableServiceErrorWithClassName() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("validateToken");
        class ServiceUnavailableException extends RuntimeException {
            public ServiceUnavailableException(String message) {
                super(message);
            }
        }
        Exception exception = new ServiceUnavailableException("Auth service down");

        // When
        for (int i = 0; i < 50; i++) {
            aspect.logRpcCallError(joinPoint, exception);
        }

        // Then
        assertThat(listAppender.list)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "UNAVAILABLE", "validateToken");
    }

    @Test
    @DisplayName("Doit logger un timeout avec DEADLINE_EXCEEDED")
    void shouldLogTimeoutError() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("validateToken");
        class RequestDEADLINE_EXCEEDEDError extends RuntimeException {
            public RequestDEADLINE_EXCEEDEDError(String message) {
                super(message);
            }
        }
        Exception exception = new RequestDEADLINE_EXCEEDEDError("Request timeout");

        // When
        for (int i = 0; i < 50; i++) {
            aspect.logRpcCallError(joinPoint, exception);
        }

        // Then
        assertThat(listAppender.list)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Timeout", "validateToken");
    }

    @Test
    @DisplayName("Doit logger un timeout avec Timeout dans le nom de classe")
    void shouldLogTimeoutErrorWithTimeoutClassName() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("generateTemporaryToken");
        class RequestTimeoutException extends RuntimeException {
            public RequestTimeoutException(String message) {
                super(message);
            }
        }
        Exception exception = new RequestTimeoutException("Request timed out after 5s");

        // When
        for (int i = 0; i < 50; i++) {
            aspect.logRpcCallError(joinPoint, exception);
        }

        // Then
        assertThat(listAppender.list)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Timeout", "generateTemporaryToken");
    }

    @Test
    @DisplayName("Doit logger une IllegalArgumentException comme warning")
    void shouldLogIllegalArgumentException() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("generateTemporaryToken");
        Exception exception = new IllegalArgumentException("Invalid email format");

        // When
        for (int i = 0; i < 50; i++) {
            aspect.logRpcCallError(joinPoint, exception);
        }

        // Then
        assertThat(listAppender.list)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Invalid argument", "generateTemporaryToken");
    }

    @Test
    @DisplayName("Doit logger une SecurityException comme erreur")
    void shouldLogSecurityException() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("validateToken");
        Exception exception = new SecurityException("Unauthorized access");

        // When
        for (int i = 0; i < 50; i++) {
            aspect.logRpcCallError(joinPoint, exception);
        }

        // Then
        assertThat(listAppender.list)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Security error", "validateToken");
    }

    @Test
    @DisplayName("Doit logger une erreur générique")
    void shouldLogGenericError() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("generateTemporaryToken");
        Exception exception = new RuntimeException("Unknown error");

        // When
        for (int i = 0; i < 50; i++) {
            aspect.logRpcCallError(joinPoint, exception);
        }

        // Then
        assertThat(listAppender.list)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-CLIENT", "Error during", "RuntimeException");
    }
}

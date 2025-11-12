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
 * Tests unitaires pour {@link GrpcServerLoggingAspect}.
 * <p>
 * Vérifie le logging correct du serveur gRPC pour les appels RPC entrants
 * depuis le module Auth, incluant démarrage/arrêt du serveur et traitement des requêtes.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GrpcServerLoggingAspect - Tests du logging gRPC serveur")
class GrpcServerLoggingAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private GrpcServerLoggingAspect aspect;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(GrpcServerLoggingAspect.class);
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
    // Tests - Démarrage du serveur gRPC
    // ========================================================================

    @Test
    @DisplayName("Doit logger avant le démarrage du serveur")
    void shouldLogBeforeServerStart() {
        // When
        aspect.logBeforeServerStart(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC", "Initializing gRPC server");
    }

    @Test
    @DisplayName("Doit logger après un démarrage réussi")
    void shouldLogAfterSuccessfulServerStart() {
        // When
        aspect.logAfterServerStart(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC", "started successfully");
    }

    @Test
    @DisplayName("Doit logger les erreurs de démarrage")
    void shouldLogServerStartError() {
        // Given
        Exception exception = new IllegalStateException("Port already in use");

        // When
        for (int i = 0; i < 50; i++) {
            aspect.logServerStartError(joinPoint, exception);
        }

        // Then
        assertThat(listAppender.list)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC", "Failed to start", "IllegalStateException");
    }

    // ========================================================================
    // Tests - Arrêt du serveur gRPC
    // ========================================================================

    @Test
    @DisplayName("Doit logger avant l'arrêt du serveur")
    void shouldLogBeforeServerStop() {
        // When
        aspect.logBeforeServerStop(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC", "graceful shutdown");
    }

    @Test
    @DisplayName("Doit logger après un arrêt réussi")
    void shouldLogAfterSuccessfulServerStop() {
        // When
        aspect.logAfterServerStop(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC", "stopped successfully");
    }

    // ========================================================================
    // Tests - Appels RPC entrants (getMemberConfirmationStatus)
    // ========================================================================

    @Test
    @DisplayName("Doit logger avant getMemberConfirmationStatus")
    void shouldLogBeforeGetMemberConfirmationStatus() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("getMemberConfirmationStatus");

        // When
        aspect.logBeforeRpcCall(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-RPC", "Received", "getMemberConfirmationStatus", "Auth module");
    }

    @Test
    @DisplayName("Doit logger après getMemberConfirmationStatus réussi")
    void shouldLogAfterSuccessfulGetMemberConfirmationStatus() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("getMemberConfirmationStatus");

        // When
        aspect.logAfterSuccessfulRpcCall(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-RPC", "getMemberConfirmationStatus", "completed successfully");
    }

    // ========================================================================
    // Tests - Appels RPC entrants (autres méthodes en DEBUG)
    // ========================================================================

    @Test
    @DisplayName("Doit logger avant un RPC standard en DEBUG")
    void shouldLogBeforeStandardRpcCall() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("checkMemberExists");

        // When
        aspect.logBeforeRpcCall(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.DEBUG);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-RPC", "Received", "checkMemberExists");
    }

    @Test
    @DisplayName("Doit logger après un RPC standard réussi en DEBUG")
    void shouldLogAfterSuccessfulStandardRpcCall() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("checkMemberExists");

        // When
        aspect.logAfterSuccessfulRpcCall(joinPoint);

        // Then
        assertThat(listAppender.list)
                .hasSize(1)
                .extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.DEBUG);
        
        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .contains("GRPC-RPC", "checkMemberExists", "completed successfully");
    }

    // ========================================================================
    // Tests - Gestion des erreurs RPC
    // ========================================================================

    @Test
    @DisplayName("Doit logger une IllegalArgumentException comme warning")
    void shouldLogIllegalArgumentException() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("getMemberConfirmationStatus");
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
                .contains("GRPC-RPC", "Invalid argument", "getMemberConfirmationStatus");
    }

    @Test
    @DisplayName("Doit logger une erreur générique")
    void shouldLogGenericError() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("checkMemberExists");
        Exception exception = new RuntimeException("Database error");

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
                .contains("GRPC-RPC", "Error in", "checkMemberExists", "RuntimeException");
    }

    @Test
    @DisplayName("Doit logger un NullPointerException")
    void shouldLogNullPointerException() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("getMemberConfirmationStatus");
        Exception exception = new NullPointerException("Member not found");

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
                .contains("GRPC-RPC", "Error in", "NullPointerException");
    }

    @Test
    @DisplayName("Doit logger une SecurityException")
    void shouldLogSecurityException() {
        // Given
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("checkMemberExists");
        Exception exception = new SecurityException("Unauthorized");

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
                .contains("GRPC-RPC", "Error in", "SecurityException");
    }
}

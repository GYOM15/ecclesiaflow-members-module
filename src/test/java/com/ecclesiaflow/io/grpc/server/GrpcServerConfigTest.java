package com.ecclesiaflow.io.grpc.server;

import io.grpc.Server;
import io.grpc.protobuf.services.HealthStatusManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link GrpcServerConfig}.
 * <p>
 * Teste la configuration et le cycle de vie du serveur gRPC, notamment le démarrage
 * et le shutdown gracieux lors de l'arrêt de l'application.
 * </p>
 */
class GrpcServerConfigTest {

    private GrpcServerConfig config;

    @Mock
    private MembersGrpcServiceImpl membersGrpcService;

    @Mock
    private Server mockServer;

    @Mock
    private HealthStatusManager mockHealthManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new GrpcServerConfig(membersGrpcService);
        
        // Configuration des valeurs par défaut
        ReflectionTestUtils.setField(config, "grpcServerPort", 9091);
        ReflectionTestUtils.setField(config, "shutdownTimeoutSeconds", 30);
    }

    // =====================================================
    // Tests de shutdown
    // =====================================================

    @Test
    @DisplayName("Stop doit gérer un serveur null sans erreur")
    void stopShouldHandleNullServer() {
        // Given
        ReflectionTestUtils.setField(config, "grpcServer", null);

        // When/Then - Ne doit pas lancer d'exception
        assertDoesNotThrow(() -> config.stop());
    }

    @Test
    @DisplayName("Stop doit arrêter proprement un serveur actif")
    void stopShouldStopActiveServer() throws InterruptedException {
        // Given
        when(mockServer.shutdown()).thenReturn(mockServer);
        when(mockServer.awaitTermination(anyLong(), any(TimeUnit.class))).thenReturn(true);
        ReflectionTestUtils.setField(config, "grpcServer", mockServer);
        ReflectionTestUtils.setField(config, "healthStatusManager", mockHealthManager);
        ReflectionTestUtils.setField(config, "shutdownTimeoutSeconds", 30);

        // When
        config.stop();

        // Then
        verify(mockHealthManager).enterTerminalState();
        verify(mockServer).shutdown();
        verify(mockServer).awaitTermination(30, TimeUnit.SECONDS);
        verify(mockServer, never()).shutdownNow(); // Ne doit pas forcer l'arrêt
    }

    @Test
    @DisplayName("Stop doit forcer l'arrêt si le timeout est dépassé")
    void stopShouldForceStopOnTimeout() throws InterruptedException {
        // Given - Le serveur ne se termine pas dans le délai
        when(mockServer.shutdown()).thenReturn(mockServer);
        when(mockServer.awaitTermination(30, TimeUnit.SECONDS)).thenReturn(false);
        when(mockServer.shutdownNow()).thenReturn(mockServer);
        when(mockServer.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(true);
        ReflectionTestUtils.setField(config, "grpcServer", mockServer);
        ReflectionTestUtils.setField(config, "healthStatusManager", mockHealthManager);
        ReflectionTestUtils.setField(config, "shutdownTimeoutSeconds", 30);

        // When
        config.stop();

        // Then
        verify(mockHealthManager).enterTerminalState();
        verify(mockServer).shutdown();
        verify(mockServer).awaitTermination(30, TimeUnit.SECONDS);
        verify(mockServer).shutdownNow(); // Doit forcer l'arrêt
        verify(mockServer).awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Stop doit gérer InterruptedException")
    void stopShouldHandleInterruptedException() throws InterruptedException {
        // Given
        when(mockServer.shutdown()).thenReturn(mockServer);
        when(mockServer.awaitTermination(anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException("Test interruption"));
        when(mockServer.shutdownNow()).thenReturn(mockServer);
        ReflectionTestUtils.setField(config, "grpcServer", mockServer);
        ReflectionTestUtils.setField(config, "healthStatusManager", mockHealthManager);

        // When
        config.stop();

        // Then
        verify(mockHealthManager).enterTerminalState();
        verify(mockServer).shutdown();
        verify(mockServer).shutdownNow(); // Doit forcer l'arrêt en cas d'interruption
        assertTrue(Thread.interrupted()); // Le flag interrupt doit être restauré
    }

    @Test
    @DisplayName("Stop doit gérer un timeout de 0 seconde")
    void stopShouldHandleZeroTimeout() throws InterruptedException {
        // Given
        when(mockServer.shutdown()).thenReturn(mockServer);
        when(mockServer.awaitTermination(0, TimeUnit.SECONDS)).thenReturn(false);
        when(mockServer.shutdownNow()).thenReturn(mockServer);
        when(mockServer.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(true);
        ReflectionTestUtils.setField(config, "grpcServer", mockServer);
        ReflectionTestUtils.setField(config, "healthStatusManager", mockHealthManager);
        ReflectionTestUtils.setField(config, "shutdownTimeoutSeconds", 0);

        // When
        config.stop();

        // Then
        verify(mockServer).shutdownNow(); // Doit immédiatement forcer l'arrêt
    }

    @Test
    @DisplayName("Stop doit lancer une exception si health manager échoue")
    void stopShouldPropagateHealthManagerException() {
        // Given
        doThrow(new RuntimeException("Health manager error"))
                .when(mockHealthManager).enterTerminalState();
        ReflectionTestUtils.setField(config, "grpcServer", mockServer);
        ReflectionTestUtils.setField(config, "healthStatusManager", mockHealthManager);

        // When/Then - Doit lancer l'exception
        assertThrows(RuntimeException.class, () -> config.stop());
    }

    @Test
    @DisplayName("GrpcServer bean doit retourner l'instance du serveur")
    void grpcServerBeanShouldReturnServerInstance() {
        // Given
        ReflectionTestUtils.setField(config, "grpcServer", mockServer);

        // When
        Server result = config.grpcServer();

        // Then
        assertSame(mockServer, result);
    }

    @Test
    @DisplayName("GrpcServer bean doit retourner null si le serveur n'est pas initialisé")
    void grpcServerBeanShouldReturnNullIfNotInitialized() {
        // Given
        ReflectionTestUtils.setField(config, "grpcServer", null);

        // When
        Server result = config.grpcServer();

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Doit gérer un serveur qui ne termine jamais même après force shutdown")
    void stopShouldHandleServerThatNeverTerminates() throws InterruptedException {
        // Given - Le serveur ne se termine jamais
        when(mockServer.shutdown()).thenReturn(mockServer);
        when(mockServer.awaitTermination(30, TimeUnit.SECONDS)).thenReturn(false);
        when(mockServer.shutdownNow()).thenReturn(mockServer);
        when(mockServer.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(false);
        ReflectionTestUtils.setField(config, "grpcServer", mockServer);
        ReflectionTestUtils.setField(config, "healthStatusManager", mockHealthManager);
        ReflectionTestUtils.setField(config, "shutdownTimeoutSeconds", 30);

        // When
        config.stop();

        // Then - Doit quand même terminer sans bloquer indéfiniment
        verify(mockServer).shutdownNow();
        verify(mockServer).awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Stop multiple fois ne doit pas causer d'erreur")
    void stopMultipleTimesShouldNotCauseError() throws InterruptedException {
        // Given
        when(mockServer.shutdown()).thenReturn(mockServer);
        when(mockServer.awaitTermination(anyLong(), any(TimeUnit.class))).thenReturn(true);
        ReflectionTestUtils.setField(config, "grpcServer", mockServer);
        ReflectionTestUtils.setField(config, "healthStatusManager", mockHealthManager);

        // When - Appeler stop plusieurs fois
        config.stop();
        config.stop();
        config.stop();

        // Then - Ne doit pas lancer d'exception
        verify(mockHealthManager, atLeast(1)).enterTerminalState();
        verify(mockServer, atLeast(1)).shutdown();
    }

    // =====================================================
    // Tests de start (test simple pour coverage)
    // =====================================================

    @Test
    @DisplayName("Start doit initialiser le serveur gRPC")
    void startShouldInitializeGrpcServer() throws Exception {
        // Given - Créer une vraie config avec un vrai service
        MembersGrpcServiceImpl realService = new MembersGrpcServiceImpl(null); // null OK pour ce test
        GrpcServerConfig realConfig = new GrpcServerConfig(realService);
        ReflectionTestUtils.setField(realConfig, "grpcServerPort", 0); // Port auto
        ReflectionTestUtils.setField(realConfig, "shutdownTimeoutSeconds", 5);

        try {
            // When
            realConfig.start();

            // Then
            Server server = realConfig.grpcServer();
            assertNotNull(server, "Le serveur ne doit pas être null");
            assertFalse(server.isShutdown(), "Le serveur ne doit pas être shutdown");
            assertTrue(server.getPort() > 0, "Le port doit être > 0");

        } finally {
            // Cleanup
            realConfig.stop();
        }
    }

    @Test
    @DisplayName("Start doit permettre l'accès au bean grpcServer")
    void startShouldExposeGrpcServerBean() throws Exception {
        // Given
        MembersGrpcServiceImpl realService = new MembersGrpcServiceImpl(null);
        GrpcServerConfig realConfig = new GrpcServerConfig(realService);
        ReflectionTestUtils.setField(realConfig, "grpcServerPort", 0);
        ReflectionTestUtils.setField(realConfig, "shutdownTimeoutSeconds", 5);

        try {
            // When
            realConfig.start();
            Server server = realConfig.grpcServer();

            // Then
            assertNotNull(server);
            assertFalse(server.isTerminated());

        } finally {
            realConfig.stop();
        }
    }
}

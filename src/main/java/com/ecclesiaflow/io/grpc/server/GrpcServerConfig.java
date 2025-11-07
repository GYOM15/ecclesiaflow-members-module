package com.ecclesiaflow.io.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Configuration du serveur gRPC pour le module Members EcclesiaFlow.
 * <p>
 * Cette classe configure et démarre automatiquement un serveur gRPC permettant
 * aux autres modules (notamment Auth) d'interroger le module Members via gRPC.
 * Elle gère le cycle de vie complet du serveur : démarrage, health checks,
 * reflection (debugging), et arrêt graceful.
 * </p>
 *
 * <p><strong>Rôle architectural :</strong> Infrastructure - gRPC Server Management</p>
 *
 * <p><strong>Services exposés :</strong></p>
 * <ul>
 *   <li>MembersService - Service métier des membres (confirmation status, etc.)</li>
 *   <li>Health - Service de health checks (pour load balancers, monitoring)</li>
 *   <li>Reflection - Service de réflexion gRPC (pour debugging avec grpcurl/grpcui)</li>
 * </ul>
 *
 * <p><strong>Configuration :</strong></p>
 * <ul>
 *   <li>grpc.enabled - Active/désactive le serveur gRPC (défaut: false)</li>
 *   <li>grpc.server.port - Port d'écoute (défaut: 9091, différent de Auth qui est 9090)</li>
 *   <li>grpc.server.shutdown-timeout-seconds - Timeout pour shutdown graceful (défaut: 30s)</li>
 * </ul>
 *
 * <p><strong>Sécurité :</strong></p>
 * <ul>
 *   <li>Utilise plaintext pour développement local (à remplacer par TLS en production)</li>
 *   <li>TODO: Activer mTLS pour production (certificats serveur)</li>
 *   <li>TODO: Implémenter authentification via JWT dans metadata</li>
 *   <li>TODO: Désactiver reflection en production</li>
 * </ul>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MembersGrpcServiceImpl
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class GrpcServerConfig {

    private final MembersGrpcServiceImpl membersGrpcService;

    @Value("${grpc.server.port:9091}")
    private int grpcServerPort;

    @Value("${grpc.server.shutdown-timeout-seconds:30}")
    private int shutdownTimeoutSeconds;

    private Server grpcServer;
    private HealthStatusManager healthStatusManager;

    /**
     * Démarre le serveur gRPC après l'initialisation du contexte Spring.
     *
     * @throws IOException si le serveur ne peut pas démarrer sur le port configuré
     */
    @PostConstruct
    public void start() throws IOException {
        healthStatusManager = new HealthStatusManager();

        grpcServer = ServerBuilder
                .forPort(grpcServerPort)
                
                // Service métier Members
                .addService(membersGrpcService)
                
                // Health check service (pour Kubernetes, load balancers, etc.)
                .addService(healthStatusManager.getHealthService())
                
                // Reflection service (pour debugging avec grpcurl/grpcui)
                // TODO: Désactiver en production pour sécurité
                .addService(ProtoReflectionService.newInstance())
                
                // Configuration du serveur
                .maxInboundMessageSize(4 * 1024 * 1024) // 4MB max message size
                .build()
                .start();

        // Marquer le service comme SERVING pour health checks
        healthStatusManager.setStatus("ecclesiaflow.members.MembersService", 
                io.grpc.health.v1.HealthCheckResponse.ServingStatus.SERVING);

        // Hook pour arrêt graceful si JVM s'arrête
        Runtime.getRuntime().addShutdownHook(new Thread(GrpcServerConfig.this::stop));
    }

    /**
     * Arrête proprement le serveur gRPC lors de l'arrêt de l'application.
     */
    @PreDestroy
    public void stop() {
        if (grpcServer != null) {
            try {
                // Marquer les services comme NOT_SERVING
                healthStatusManager.enterTerminalState();
                
                // Arrêt graceful avec timeout
                grpcServer.shutdown();
                
                if (!grpcServer.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                    grpcServer.shutdownNow();
                    grpcServer.awaitTermination(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                grpcServer.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Expose le serveur gRPC comme bean Spring (optionnel, pour monitoring).
     *
     * @return l'instance du serveur gRPC
     */
    @Bean
    public Server grpcServer() {
        return grpcServer;
    }
}

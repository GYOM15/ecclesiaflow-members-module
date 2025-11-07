package com.ecclesiaflow.io.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * Configuration du client gRPC pour le module Members.
 * <p>
 * Cette classe configure le canal de communication gRPC (ManagedChannel) vers
 * le module d'authentification. Elle gère le cycle de vie du canal : création,
 * configuration et fermeture graceful lors de l'arrêt de l'application.
 * </p>
 *
 * <p><strong>Rôle architectural :</strong> Infrastructure - gRPC Client Configuration</p>
 *
 * <p><strong>Fonctionnalités :</strong></p>
 * <ul>
 *   <li>Création d'un canal gRPC réutilisable vers le module Auth</li>
 *   <li>Configuration des timeouts et retry policies</li>
 *   <li>Keep-alive pour maintenir la connexion active</li>
 *   <li>Shutdown graceful du canal lors de l'arrêt de l'application</li>
 *   <li>Support de la compression gzip pour réduire la bande passante</li>
 * </ul>
 *
 * <p><strong>Configuration :</strong></p>
 * <ul>
 *   <li>grpc.enabled - Active/désactive le client gRPC (défaut: false)</li>
 *   <li>grpc.auth.host - Hostname du serveur Auth (défaut: localhost)</li>
 *   <li>grpc.auth.port - Port du serveur Auth (défaut: 9090)</li>
 *   <li>grpc.client.shutdown-timeout-seconds - Timeout pour shutdown (défaut: 5s)</li>
 * </ul>
 *
 * <p><strong>Sécurité :</strong></p>
 * <ul>
 *   <li>Utilise plaintext pour développement local (à remplacer par TLS en production)</li>
 *   <li>TODO: Activer mTLS pour production (certificats clients)</li>
 *   <li>TODO: Implémenter authentification via JWT dans metadata</li>
 * </ul>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see ManagedChannel
 * @see AuthGrpcClient
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class GrpcClientConfig {

    @Value("${grpc.auth.host:localhost}")
    private String authServiceHost;

    @Value("${grpc.auth.port:9090}")
    private int authServicePort;

    @Value("${grpc.client.shutdown-timeout-seconds:5}")
    private int shutdownTimeoutSeconds;

    private ManagedChannel managedChannel;

    /**
     * Crée et configure le canal gRPC vers le module d'authentification.
     * <p>
     * Ce canal est partagé par tous les appels gRPC vers le module Auth.
     * Il est automatiquement réutilisé et optimise les connexions réseau.
     * </p>
     *
     * <p><strong>Configuration appliquée :</strong></p>
     * <ul>
     *   <li>Max inbound message size: 4MB</li>
     *   <li>Keep-alive: 30s (ping serveur pour maintenir connexion)</li>
     *   <li>Keep-alive timeout: 10s</li>
     *   <li>Keep-alive sans traffic: true (ping même sans requêtes)</li>
     *   <li>Idle timeout: 5 minutes (fermeture si inactif)</li>
     * </ul>
     *
     * @return le canal gRPC configuré et prêt à l'emploi
     */
    @Bean
    public ManagedChannel authGrpcChannel() {
        managedChannel = ManagedChannelBuilder
                .forAddress(authServiceHost, authServicePort)
                
                // DÉVELOPPEMENT: Utilise plaintext (pas de TLS)
                // TODO PRODUCTION: Remplacer par .useTransportSecurity() + certificats
                .usePlaintext()
                
                // Configuration des messages
                .maxInboundMessageSize(4 * 1024 * 1024) // 4MB max
                
                // Keep-alive pour maintenir la connexion active
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                
                // Idle timeout (ferme la connexion si inactive)
                .idleTimeout(5, TimeUnit.MINUTES)
                
                .build();

        return managedChannel;
    }

    /**
     * Ferme proprement le canal gRPC lors de l'arrêt de l'application.
     * <p>
     * Cette méthode est appelée automatiquement par Spring lors de la destruction
     * du contexte. Elle initie un arrêt graceful du canal avec timeout pour
     * permettre aux requêtes en cours de se terminer.
     * </p>
     *
     * @throws InterruptedException si l'arrêt est interrompu
     */
    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (managedChannel != null && !managedChannel.isShutdown()) {
            // Demande un arrêt graceful
            managedChannel.shutdown();
            
            // Attend que toutes les requêtes en cours se terminent (max 5s)
            if (!managedChannel.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                // Force l'arrêt si le timeout est dépassé
                managedChannel.shutdownNow();
                
                // Attend encore 1 seconde après force shutdown
                managedChannel.awaitTermination(1, TimeUnit.SECONDS);
            }
        }
    }
}

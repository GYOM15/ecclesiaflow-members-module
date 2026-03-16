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
                .keepAliveTime(120, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(false)
                
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

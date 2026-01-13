package com.ecclesiaflow.application.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * gRPC channel configuration for Email service communication.
 *
 * <p>Provides the {@link ManagedChannel} bean used by EmailGrpcClient.
 * The client itself is a Spring component with circuit breaker support.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.io.communication.email.EmailGrpcClient
 */
@Configuration
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true")
public class GrpcEmailClientConfig {

    private final String emailHost;
    private final int emailPort;
    private final int shutdownTimeoutSeconds;
    private ManagedChannel emailGrpcChannel;

    public GrpcEmailClientConfig(
            @Value("${grpc.email.host:localhost}") String emailHost,
            @Value("${grpc.email.port:9092}") int emailPort,
            @Value("${grpc.client.shutdown-timeout-seconds:5}") int shutdownTimeoutSeconds) {
        this.emailHost = emailHost;
        this.emailPort = emailPort;
        this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
    }

    @Bean
    public ManagedChannel emailGrpcChannel() {
        this.emailGrpcChannel = ManagedChannelBuilder
                .forAddress(emailHost, emailPort)
                .usePlaintext() // TODO: Activer TLS en production
                .build();
        
        return this.emailGrpcChannel;
    }

    @PreDestroy
    public void shutdown() {
        if (emailGrpcChannel != null && !emailGrpcChannel.isShutdown()) {
            try {
                emailGrpcChannel.shutdown();
                if (!emailGrpcChannel.awaitTermination(shutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                    emailGrpcChannel.shutdownNow();
                    emailGrpcChannel.awaitTermination(2, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                emailGrpcChannel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

package com.ecclesiaflow.io.grpc.client;

import com.ecclesiaflow.grpc.auth.AuthServiceGrpc;
import com.ecclesiaflow.grpc.auth.TemporaryTokenRequest;
import com.ecclesiaflow.grpc.auth.TemporaryTokenResponse;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour AuthGrpcClient.
 * <p>
 * Teste le client gRPC avec un vrai serveur Auth mock via InProcessServer.
 * Couvre la communication réelle et les scénarios d'erreur.
 * </p>
 */
class AuthGrpcClientIntegrationTest {

    private Server server;
    private ManagedChannel channel;
    private AuthGrpcClient client;

    private static final String SERVER_NAME = "test-auth-grpc-client";
    private static final String EMAIL = "test@example.com";
    private static final UUID MEMBER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String GENERATED_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.token";
    private static final String INVALID_EMAIL = "invalid@example.com";

    @BeforeEach
    void setUp() throws Exception {
        // Créer un service Auth mock
        AuthServiceGrpc.AuthServiceImplBase mockService = new AuthServiceGrpc.AuthServiceImplBase() {
            @Override
            public void generateTemporaryToken(TemporaryTokenRequest request,
                                                StreamObserver<TemporaryTokenResponse> responseObserver) {
                String email = request.getEmail();
                String memberId = request.getMemberId();

                if (email.isEmpty()) {
                    responseObserver.onError(new StatusRuntimeException(
                            Status.INVALID_ARGUMENT.withDescription("Email cannot be null or empty")));
                } else if (memberId.isEmpty()) {
                    responseObserver.onError(new StatusRuntimeException(
                            Status.INVALID_ARGUMENT.withDescription("MemberId cannot be null or empty")));
                } else if (email.equals(INVALID_EMAIL)) {
                    responseObserver.onError(new StatusRuntimeException(
                            Status.INTERNAL.withDescription("Failed to generate token")));
                } else {
                    try {
                        UUID.fromString(memberId); // Validate UUID
                        responseObserver.onNext(TemporaryTokenResponse.newBuilder()
                                .setTemporaryToken(GENERATED_TOKEN)
                                .build());
                        responseObserver.onCompleted();
                    } catch (IllegalArgumentException e) {
                        responseObserver.onError(new StatusRuntimeException(
                                Status.INVALID_ARGUMENT.withDescription("Invalid UUID format: " + memberId)));
                    }
                }
            }
        };

        // Démarrer le serveur in-memory
        server = InProcessServerBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .addService(mockService)
                .build()
                .start();

        // Créer le canal client
        channel = InProcessChannelBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .build();

        // Créer le client
        client = new AuthGrpcClient(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    @DisplayName("Should generate temporary token successfully via real gRPC call")
    void retrievePostActivationToken_Success() {
        // When
        String token = client.retrievePostActivationToken(EMAIL, MEMBER_ID);

        // Then
        assertNotNull(token);
        assertEquals(GENERATED_TOKEN, token);
    }

    @Test
    @DisplayName("Should throw exception for empty email")
    void retrievePostActivationToken_EmptyEmail() {
        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            client.retrievePostActivationToken("", MEMBER_ID);
        });

        assertTrue(exception.getMessage().contains("Email cannot be null or empty") ||
                   exception instanceof StatusRuntimeException);
    }

    @Test
    @DisplayName("Should handle special UUID values")
    void retrievePostActivationToken_SpecialUUID() {
        // Given - Using zero UUID which is valid but special
        UUID zeroUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        // When
        String token = client.retrievePostActivationToken(EMAIL, zeroUuid);

        // Then - Should still generate token for any valid UUID
        assertNotNull(token);
        assertEquals(GENERATED_TOKEN, token);
    }

    @Test
    @DisplayName("Should throw exception when service returns error")
    void retrievePostActivationToken_ServiceError() {
        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            client.retrievePostActivationToken(INVALID_EMAIL, MEMBER_ID);
        });

        assertTrue(exception.getMessage().contains("Failed to generate token") ||
                   exception instanceof StatusRuntimeException);
    }

    @Test
    @DisplayName("Should handle multiple sequential requests")
    void retrievePostActivationToken_MultipleRequests() {
        // When/Then
        String token1 = client.retrievePostActivationToken(EMAIL, MEMBER_ID);
        String token2 = client.retrievePostActivationToken(EMAIL, MEMBER_ID);
        String token3 = client.retrievePostActivationToken(EMAIL, MEMBER_ID);

        // All should succeed
        assertEquals(GENERATED_TOKEN, token1);
        assertEquals(GENERATED_TOKEN, token2);
        assertEquals(GENERATED_TOKEN, token3);
    }

    @Test
    @DisplayName("Should handle concurrent requests safely")
    void retrievePostActivationToken_ConcurrentRequests() throws InterruptedException {
        // Given
        Thread[] threads = new Thread[10];

        // When
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                String token = client.retrievePostActivationToken(EMAIL, MEMBER_ID);
                assertEquals(GENERATED_TOKEN, token);
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - All requests should complete without errors
    }

    @Test
    @DisplayName("Should validate different member IDs")
    void retrievePostActivationToken_DifferentMemberIds() {
        // Given
        UUID memberId1 = UUID.randomUUID();
        UUID memberId2 = UUID.randomUUID();

        // When
        String token1 = client.retrievePostActivationToken(EMAIL, memberId1);
        String token2 = client.retrievePostActivationToken(EMAIL, memberId2);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertEquals(token1, token2); // Mock returns same token
    }

    @Test
    @DisplayName("Should validate channel is connected")
    void channelIsConnected() {
        assertNotNull(channel);
        assertFalse(channel.isShutdown());
        assertFalse(channel.isTerminated());
    }

    @Test
    @DisplayName("Should handle UNAVAILABLE status correctly")
    void retrievePostActivationToken_Unavailable() throws Exception {
        // Arrange - Create a service that returns UNAVAILABLE
        AuthServiceGrpc.AuthServiceImplBase unavailableService = new AuthServiceGrpc.AuthServiceImplBase() {
            @Override
            public void generateTemporaryToken(TemporaryTokenRequest request,
                                                StreamObserver<TemporaryTokenResponse> responseObserver) {
                responseObserver.onError(new StatusRuntimeException(
                        Status.UNAVAILABLE.withDescription("Auth service is down")));
            }
        };

        // Shutdown previous server and create new one
        if (server != null) server.shutdownNow();
        if (channel != null) channel.shutdownNow();

        server = InProcessServerBuilder.forName("unavailable-test")
                .directExecutor()
                .addService(unavailableService)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName("unavailable-test")
                .directExecutor()
                .build();

        client = new AuthGrpcClient(channel);

        // Act/Assert
        AuthGrpcClient.AuthServiceUnavailableException exception =
                assertThrows(AuthGrpcClient.AuthServiceUnavailableException.class, () -> {
                    client.retrievePostActivationToken(EMAIL, MEMBER_ID);
                });

        assertTrue(exception.getMessage().contains("Auth service is unavailable"));
    }

    @Test
    @DisplayName("Should handle DEADLINE_EXCEEDED status correctly")
    void retrievePostActivationToken_DeadlineExceeded() throws Exception {
        // Arrange
        AuthServiceGrpc.AuthServiceImplBase timeoutService = new AuthServiceGrpc.AuthServiceImplBase() {
            @Override
            public void generateTemporaryToken(TemporaryTokenRequest request,
                                                StreamObserver<TemporaryTokenResponse> responseObserver) {
                responseObserver.onError(new StatusRuntimeException(
                        Status.DEADLINE_EXCEEDED.withDescription("Request timeout after 5 seconds")));
            }
        };

        if (server != null) server.shutdownNow();
        if (channel != null) channel.shutdownNow();

        server = InProcessServerBuilder.forName("timeout-test")
                .directExecutor()
                .addService(timeoutService)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName("timeout-test")
                .directExecutor()
                .build();

        client = new AuthGrpcClient(channel);

        // Act/Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            client.retrievePostActivationToken(EMAIL, MEMBER_ID);
        });

        assertTrue(exception.getMessage().contains("Timeout exceeded"));
    }

    @Test
    @DisplayName("Should handle INVALID_ARGUMENT status correctly")
    void retrievePostActivationToken_InvalidArgument() throws Exception {
        // Arrange
        AuthServiceGrpc.AuthServiceImplBase invalidArgService = new AuthServiceGrpc.AuthServiceImplBase() {
            @Override
            public void generateTemporaryToken(TemporaryTokenRequest request,
                                                StreamObserver<TemporaryTokenResponse> responseObserver) {
                responseObserver.onError(new StatusRuntimeException(
                        Status.INVALID_ARGUMENT.withDescription("Invalid email format")));
            }
        };

        if (server != null) server.shutdownNow();
        if (channel != null) channel.shutdownNow();

        server = InProcessServerBuilder.forName("invalid-arg-test")
                .directExecutor()
                .addService(invalidArgService)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName("invalid-arg-test")
                .directExecutor()
                .build();

        client = new AuthGrpcClient(channel);

        // Act/Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            client.retrievePostActivationToken(EMAIL, MEMBER_ID);
        });

        assertTrue(exception.getMessage().contains("Invalid argument"));
    }

    @Test
    @DisplayName("Should handle UNAUTHENTICATED status correctly")
    void retrievePostActivationToken_Unauthenticated() throws Exception {
        // Arrange
        AuthServiceGrpc.AuthServiceImplBase unauthService = new AuthServiceGrpc.AuthServiceImplBase() {
            @Override
            public void generateTemporaryToken(TemporaryTokenRequest request,
                                                StreamObserver<TemporaryTokenResponse> responseObserver) {
                responseObserver.onError(new StatusRuntimeException(
                        Status.UNAUTHENTICATED.withDescription("Authentication required")));
            }
        };

        if (server != null) server.shutdownNow();
        if (channel != null) channel.shutdownNow();

        server = InProcessServerBuilder.forName("unauth-test")
                .directExecutor()
                .addService(unauthService)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName("unauth-test")
                .directExecutor()
                .build();

        client = new AuthGrpcClient(channel);

        // Act/Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            client.retrievePostActivationToken(EMAIL, MEMBER_ID);
        });

        assertTrue(exception.getMessage().contains("Authentication failed"));
    }

    @Test
    @DisplayName("Should handle UNKNOWN status as default case")
    void retrievePostActivationToken_UnknownStatus() throws Exception {
        // Arrange
        AuthServiceGrpc.AuthServiceImplBase unknownService = new AuthServiceGrpc.AuthServiceImplBase() {
            @Override
            public void generateTemporaryToken(TemporaryTokenRequest request,
                                                StreamObserver<TemporaryTokenResponse> responseObserver) {
                responseObserver.onError(new StatusRuntimeException(
                        Status.UNKNOWN.withDescription("Something unexpected happened")));
            }
        };

        if (server != null) server.shutdownNow();
        if (channel != null) channel.shutdownNow();

        server = InProcessServerBuilder.forName("unknown-test")
                .directExecutor()
                .addService(unknownService)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName("unknown-test")
                .directExecutor()
                .build();

        client = new AuthGrpcClient(channel);

        // Act/Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            client.retrievePostActivationToken(EMAIL, MEMBER_ID);
        });

        assertTrue(exception.getMessage().contains("Unexpected gRPC error"));
        assertTrue(exception.getMessage().contains("UNKNOWN"));
    }
}

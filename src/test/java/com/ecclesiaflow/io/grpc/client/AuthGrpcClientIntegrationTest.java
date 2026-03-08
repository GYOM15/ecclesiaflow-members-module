package com.ecclesiaflow.io.grpc.client;

import com.ecclesiaflow.business.domain.auth.PasswordSetupTokenResponse;
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
 * Integration tests for AuthGrpcClient.
 * Tests the gRPC client with a real Auth mock via InProcessServer.
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
                        UUID.fromString(memberId);
                        responseObserver.onNext(TemporaryTokenResponse.newBuilder()
                                .setTemporaryToken(GENERATED_TOKEN)
                                .setExpiresInSeconds(900)
                                .setPasswordEndpoint("/ecclesiaflow/auth/password")
                                .build());
                        responseObserver.onCompleted();
                    } catch (IllegalArgumentException e) {
                        responseObserver.onError(new StatusRuntimeException(
                                Status.INVALID_ARGUMENT.withDescription("Invalid UUID format: " + memberId)));
                    }
                }
            }
        };

        server = InProcessServerBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .addService(mockService)
                .build()
                .start();

        channel = InProcessChannelBuilder
                .forName(SERVER_NAME)
                .directExecutor()
                .build();

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
        PasswordSetupTokenResponse response = client.retrievePostActivationToken(EMAIL, MEMBER_ID);

        assertNotNull(response);
        assertEquals(GENERATED_TOKEN, response.token());
        assertEquals(900, response.expiresInSeconds());
        assertEquals("/ecclesiaflow/auth/password", response.passwordEndpoint());
    }

    @Test
    @DisplayName("Should throw exception for empty email")
    void retrievePostActivationToken_EmptyEmail() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            client.retrievePostActivationToken("", MEMBER_ID)
        );

        assertTrue(exception.getMessage().contains("Email cannot be null or empty") ||
                   exception instanceof StatusRuntimeException);
    }

    @Test
    @DisplayName("Should handle special UUID values")
    void retrievePostActivationToken_SpecialUUID() {
        UUID zeroUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        PasswordSetupTokenResponse response = client.retrievePostActivationToken(EMAIL, zeroUuid);

        assertNotNull(response);
        assertEquals(GENERATED_TOKEN, response.token());
    }

    @Test
    @DisplayName("Should throw exception when service returns error")
    void retrievePostActivationToken_ServiceError() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            client.retrievePostActivationToken(INVALID_EMAIL, MEMBER_ID)
        );

        assertTrue(exception.getMessage().contains("Failed to generate token") ||
                   exception instanceof StatusRuntimeException);
    }

    @Test
    @DisplayName("Should handle multiple sequential requests")
    void retrievePostActivationToken_MultipleRequests() {
        PasswordSetupTokenResponse r1 = client.retrievePostActivationToken(EMAIL, MEMBER_ID);
        PasswordSetupTokenResponse r2 = client.retrievePostActivationToken(EMAIL, MEMBER_ID);
        PasswordSetupTokenResponse r3 = client.retrievePostActivationToken(EMAIL, MEMBER_ID);

        assertEquals(GENERATED_TOKEN, r1.token());
        assertEquals(GENERATED_TOKEN, r2.token());
        assertEquals(GENERATED_TOKEN, r3.token());
    }

    @Test
    @DisplayName("Should handle concurrent requests safely")
    void retrievePostActivationToken_ConcurrentRequests() throws InterruptedException {
        Thread[] threads = new Thread[10];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                PasswordSetupTokenResponse response = client.retrievePostActivationToken(EMAIL, MEMBER_ID);
                assertEquals(GENERATED_TOKEN, response.token());
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    @DisplayName("Should validate different member IDs")
    void retrievePostActivationToken_DifferentMemberIds() {
        UUID memberId1 = UUID.randomUUID();
        UUID memberId2 = UUID.randomUUID();

        PasswordSetupTokenResponse r1 = client.retrievePostActivationToken(EMAIL, memberId1);
        PasswordSetupTokenResponse r2 = client.retrievePostActivationToken(EMAIL, memberId2);

        assertNotNull(r1);
        assertNotNull(r2);
        assertEquals(r1.token(), r2.token()); // Mock returns same token
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
        AuthServiceGrpc.AuthServiceImplBase unavailableService = new AuthServiceGrpc.AuthServiceImplBase() {
            @Override
            public void generateTemporaryToken(TemporaryTokenRequest request,
                                                StreamObserver<TemporaryTokenResponse> responseObserver) {
                responseObserver.onError(new StatusRuntimeException(
                        Status.UNAVAILABLE.withDescription("Auth service is down")));
            }
        };

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

        AuthGrpcClient.AuthServiceUnavailableException exception =
                assertThrows(AuthGrpcClient.AuthServiceUnavailableException.class, () ->
                    client.retrievePostActivationToken(EMAIL, MEMBER_ID)
                );

        assertTrue(exception.getMessage().contains("Auth service is unavailable"));
    }

    @Test
    @DisplayName("Should handle DEADLINE_EXCEEDED status correctly")
    void retrievePostActivationToken_DeadlineExceeded() throws Exception {
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

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            client.retrievePostActivationToken(EMAIL, MEMBER_ID)
        );

        assertTrue(exception.getMessage().contains("Timeout exceeded"));
    }

    @Test
    @DisplayName("Should handle INVALID_ARGUMENT status correctly")
    void retrievePostActivationToken_InvalidArgument() throws Exception {
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

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            client.retrievePostActivationToken(EMAIL, MEMBER_ID)
        );

        assertTrue(exception.getMessage().contains("Invalid argument"));
    }

    @Test
    @DisplayName("Should handle UNAUTHENTICATED status correctly")
    void retrievePostActivationToken_Unauthenticated() throws Exception {
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

        SecurityException exception = assertThrows(SecurityException.class, () ->
            client.retrievePostActivationToken(EMAIL, MEMBER_ID)
        );

        assertTrue(exception.getMessage().contains("Authentication failed"));
    }

    @Test
    @DisplayName("Should handle UNKNOWN status as default case")
    void retrievePostActivationToken_UnknownStatus() throws Exception {
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

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            client.retrievePostActivationToken(EMAIL, MEMBER_ID)
        );

        assertTrue(exception.getMessage().contains("Unexpected gRPC error"));
        assertTrue(exception.getMessage().contains("UNKNOWN"));
    }
}

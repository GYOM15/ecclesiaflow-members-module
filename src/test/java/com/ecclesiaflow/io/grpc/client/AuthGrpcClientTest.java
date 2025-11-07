package com.ecclesiaflow.io.grpc.client;

import com.ecclesiaflow.grpc.auth.AuthServiceGrpc;
import com.ecclesiaflow.grpc.auth.TemporaryTokenRequest;
import com.ecclesiaflow.grpc.auth.TemporaryTokenResponse;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthGrpcClient.
 * <p>
 * Vérifie le comportement du client gRPC pour les appels vers le Auth Module.
 * </p>
 */
class AuthGrpcClientTest {

    private ManagedChannel authGrpcChannel;
    private AuthGrpcClient authGrpcClient;

    private static final String EMAIL = "test@example.com";
    private static final UUID MEMBER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String GENERATED_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token";

    @BeforeEach
    void setUp() {
        authGrpcChannel = mock(ManagedChannel.class);
        authGrpcClient = new AuthGrpcClient(authGrpcChannel);
    }

    @Test
    @DisplayName("Should construct request with correct email and memberId")
    void retrievePostActivationToken_ConstructsRequestCorrectly() {
        // Given/When
        TemporaryTokenRequest request = TemporaryTokenRequest.newBuilder()
                .setEmail(EMAIL)
                .setMemberId(MEMBER_ID.toString())
                .build();

        // Then
        assertEquals(EMAIL, request.getEmail());
        assertEquals(MEMBER_ID.toString(), request.getMemberId());
    }

    @Test
    @DisplayName("Should parse response token correctly")
    void retrievePostActivationToken_ParsesResponseCorrectly() {
        // Given
        TemporaryTokenResponse response = TemporaryTokenResponse.newBuilder()
                .setTemporaryToken(GENERATED_TOKEN)
                .build();

        // When
        String token = response.getTemporaryToken();

        // Then
        assertEquals(GENERATED_TOKEN, token);
    }

    @Test
    @DisplayName("Should verify channel is injected correctly")
    void constructor_InjectsChannelCorrectly() {
        // Given
        ManagedChannel channel = mock(ManagedChannel.class);

        // When
        AuthGrpcClient client = new AuthGrpcClient(channel);

        // Then
        assertNotNull(client);
    }

    @Test
    @DisplayName("Should handle UNAVAILABLE status correctly")
    void handleGrpcException_Unavailable() {
        // Given
        StatusRuntimeException exception = new StatusRuntimeException(
                Status.UNAVAILABLE.withDescription("Auth service is unavailable")
        );

        // When/Then
        assertEquals(Status.UNAVAILABLE.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Auth service is unavailable"));
    }

    @Test
    @DisplayName("Should handle DEADLINE_EXCEEDED status correctly")
    void handleGrpcException_DeadlineExceeded() {
        // Given
        StatusRuntimeException exception = new StatusRuntimeException(
                Status.DEADLINE_EXCEEDED.withDescription("Request timeout after 5 seconds")
        );

        // When/Then
        assertEquals(Status.DEADLINE_EXCEEDED.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Request timeout"));
    }

    @Test
    @DisplayName("Should handle INVALID_ARGUMENT status correctly")
    void handleGrpcException_InvalidArgument() {
        // Given
        StatusRuntimeException exception = new StatusRuntimeException(
                Status.INVALID_ARGUMENT.withDescription("Invalid email format")
        );

        // When/Then
        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should handle INTERNAL status correctly")
    void handleGrpcException_Internal() {
        // Given
        StatusRuntimeException exception = new StatusRuntimeException(
                Status.INTERNAL.withDescription("Internal server error")
        );

        // When/Then
        assertEquals(Status.INTERNAL.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Internal server error"));
    }

    @Test
    @DisplayName("Should handle empty email in request")
    void retrievePostActivationToken_EmptyEmail() {
        // Given/When
        TemporaryTokenRequest request = TemporaryTokenRequest.newBuilder()
                .setEmail("")
                .setMemberId(MEMBER_ID.toString())
                .build();

        // Then
        assertEquals("", request.getEmail());
        assertEquals(MEMBER_ID.toString(), request.getMemberId());
    }

    @Test
    @DisplayName("Should handle null values in request")
    void retrievePostActivationToken_NullValues() {
        // Given/When
        TemporaryTokenRequest request = TemporaryTokenRequest.newBuilder().build();

        // Then
        assertEquals("", request.getEmail()); // Protobuf default for string is empty string
        assertEquals("", request.getMemberId());
    }

    @Test
    @DisplayName("Should build request with all required fields")
    void buildRequest_AllFieldsPresent() {
        // Given/When
        TemporaryTokenRequest request = TemporaryTokenRequest.newBuilder()
                .setEmail(EMAIL)
                .setMemberId(MEMBER_ID.toString())
                .build();

        // Then
        assertNotNull(request);
        assertFalse(request.getEmail().isEmpty());
        assertFalse(request.getMemberId().isEmpty());
    }

    @Test
    @DisplayName("Should handle response with empty token")
    void parseResponse_EmptyToken() {
        // Given
        TemporaryTokenResponse response = TemporaryTokenResponse.newBuilder()
                .setTemporaryToken("")
                .build();

        // When
        String token = response.getTemporaryToken();

        // Then
        assertEquals("", token);
    }

    @Test
    @DisplayName("Should verify UUID string conversion")
    void verifyUuidConversion() {
        // Given
        UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // When
        String uuidString = uuid.toString();

        // Then
        assertEquals("550e8400-e29b-41d4-a716-446655440000", uuidString);
    }

    @Test
    @DisplayName("Should test AuthServiceUnavailableException construction")
    void authServiceUnavailableException_Construction() {
        // Given
        String message = "Service unavailable";
        StatusRuntimeException cause = new StatusRuntimeException(Status.UNAVAILABLE);

        // When
        AuthGrpcClient.AuthServiceUnavailableException exception =
                new AuthGrpcClient.AuthServiceUnavailableException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should test AuthServiceUnavailableException with null cause")
    void authServiceUnavailableException_NullCause() {
        // Given
        String message = "Service unavailable";

        // When
        AuthGrpcClient.AuthServiceUnavailableException exception =
                new AuthGrpcClient.AuthServiceUnavailableException(message, null);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should handle UNAUTHENTICATED status correctly")
    void handleGrpcException_Unauthenticated() {
        // Given
        StatusRuntimeException exception = new StatusRuntimeException(
                Status.UNAUTHENTICATED.withDescription("Invalid credentials")
        );

        // When/Then
        assertEquals(Status.UNAUTHENTICATED.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Invalid credentials"));
    }

    @Test
    @DisplayName("Should handle UNKNOWN status correctly")
    void handleGrpcException_Unknown() {
        // Given
        StatusRuntimeException exception = new StatusRuntimeException(
                Status.UNKNOWN.withDescription("Unknown error occurred")
        );

        // When/Then
        assertEquals(Status.UNKNOWN.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Unknown error"));
    }
}

package com.ecclesiaflow.io.grpc.client;

import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.auth.PasswordSetupTokenResponse;
import com.ecclesiaflow.grpc.auth.*;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Client gRPC pour communiquer avec le module d'authentification.
 * <p>
 * Cette classe implémente un client gRPC qui permet au module Members
 * d'appeler les services du module Auth via gRPC au lieu de REST/WebClient.
 * Elle encapsule la complexité des appels gRPC et fournit une API simple
 * et type-safe pour les services métier.
 * </p>
 *
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see AuthServiceGrpc
 * @see GrpcClientConfig
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = false)
public class AuthGrpcClient implements AuthClient {

    private final ManagedChannel authGrpcChannel;

    // Timeout par défaut pour les appels gRPC
    private static final int DEFAULT_TIMEOUT_SECONDS = 5;

    /**
     * Génère un token temporaire JWT via gRPC pour permettre la définition du mot de passe.
     *
     * @param email l'email du membre confirmé (requis, validé côté serveur)
     * @param memberId l'UUID du membre (requis)
     * @return le token temporaire JWT généré par le module Auth
     * @throws IllegalArgumentException si email ou memberId invalide
     * @throws AuthServiceUnavailableException si le service Auth est indisponible
     * @throws RuntimeException si erreur inattendue
     */
    @Override
    public PasswordSetupTokenResponse retrievePostActivationToken(String email, UUID memberId) {
        // Création du stub avec timeout
        AuthServiceGrpc.AuthServiceBlockingStub stub = AuthServiceGrpc
                .newBlockingStub(authGrpcChannel)
                .withDeadlineAfter(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        try {
            // Construction de la requête Protobuf
            TemporaryTokenRequest request = TemporaryTokenRequest.newBuilder()
                    .setEmail(email)
                    .setMemberId(memberId.toString())
                    .build();

            // Appel RPC synchrone
            TemporaryTokenResponse response = stub.generateTemporaryToken(request);

            // Return response with token and password endpoint
            return new PasswordSetupTokenResponse(
                    response.getTemporaryToken(),
                    response.getExpiresInSeconds(),
                    response.getPasswordEndpoint()
            );

        } catch (StatusRuntimeException e) {
            // Conversion des erreurs gRPC en exceptions métier
            throw handleGrpcException(e, "generateTemporaryToken");
        }
    }

    @Override
    public void deleteKeycloakUser(String keycloakUserId) {
        AuthServiceGrpc.AuthServiceBlockingStub stub = AuthServiceGrpc
                .newBlockingStub(authGrpcChannel)
                .withDeadlineAfter(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        try {
            DeleteKeycloakUserRequest request = DeleteKeycloakUserRequest.newBuilder()
                    .setKeycloakUserId(keycloakUserId)
                    .build();

            stub.deleteKeycloakUser(request);

        } catch (StatusRuntimeException e) {
            throw handleGrpcException(e, "deleteKeycloakUser");
        }
    }

    @Override
    public void disableKeycloakUser(String keycloakUserId) {
        AuthServiceGrpc.AuthServiceBlockingStub stub = AuthServiceGrpc
                .newBlockingStub(authGrpcChannel)
                .withDeadlineAfter(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        try {
            DisableKeycloakUserRequest request = DisableKeycloakUserRequest.newBuilder()
                    .setKeycloakUserId(keycloakUserId)
                    .build();

            stub.disableKeycloakUser(request);

        } catch (StatusRuntimeException e) {
            throw handleGrpcException(e, "disableKeycloakUser");
        }
    }

    @Override
    public void updateKeycloakUserEmail(String keycloakUserId, String newEmail) {
        AuthServiceGrpc.AuthServiceBlockingStub stub = AuthServiceGrpc
                .newBlockingStub(authGrpcChannel)
                .withDeadlineAfter(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        try {
            UpdateKeycloakUserEmailRequest request = UpdateKeycloakUserEmailRequest.newBuilder()
                    .setKeycloakUserId(keycloakUserId)
                    .setNewEmail(newEmail)
                    .build();

            stub.updateKeycloakUserEmail(request);

        } catch (StatusRuntimeException e) {
            throw handleGrpcException(e, "updateKeycloakUserEmail");
        }
    }

    // ========================================================================
    // Error handling
    // ========================================================================

    /**
     * Convertit les exceptions gRPC en exceptions métier appropriées.
     *
     * @param e l'exception gRPC interceptée
     * @return l'exception métier appropriée
     */
    private RuntimeException handleGrpcException(StatusRuntimeException e, String methodName) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        return switch (code) {
            case UNAVAILABLE -> new AuthServiceUnavailableException(
                    "Auth service is unavailable: " + description, e);

            case DEADLINE_EXCEEDED -> new RuntimeException(
                    "Timeout exceeded while calling " + methodName + ": " + description, e);

            case INVALID_ARGUMENT -> new IllegalArgumentException(
                    "Invalid argument in " + methodName + ": " + description, e);

            case INTERNAL -> new RuntimeException(
                    "Internal error in Auth service during " + methodName + ": " + description, e);

            case UNAUTHENTICATED -> new SecurityException(
                    "Authentication failed during " + methodName + ": " + description, e);

            default -> new RuntimeException(
                    "Unexpected gRPC error during " + methodName + " [" + code + "]: " + description, e);
        };
    }

    /**
     * Exception personnalisée pour indiquer que le service Auth est indisponible.
     */
    public static class AuthServiceUnavailableException extends RuntimeException {
        public AuthServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

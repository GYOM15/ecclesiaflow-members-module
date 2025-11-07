package com.ecclesiaflow.io.grpc.client;

import com.ecclesiaflow.business.domain.auth.AuthClient;
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
 * <p><strong>Rôle architectural :</strong> Adapter - gRPC Client to Business Logic</p>
 *
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Création et gestion des stubs gRPC (blocking/async)</li>
 *   <li>Appels RPC vers le module Auth (GenerateTemporaryToken)</li>
 *   <li>Conversion des réponses Protobuf vers types Java métier</li>
 *   <li>Gestion des erreurs gRPC et mapping vers exceptions métier</li>
 *   <li>Configuration des timeouts par appel</li>
 * </ul>
 *
 * <p><strong>Clean Architecture :</strong></p>
 * <pre>
 * Business Layer → AuthGrpcClient (Adapter) → gRPC Channel → Auth Module
 * </pre>
 *
 * <p><strong>Gestion d'erreurs :</strong></p>
 * <ul>
 *   <li>UNAVAILABLE - Service Auth indisponible → AuthServiceUnavailableException</li>
 *   <li>DEADLINE_EXCEEDED - Timeout dépassé → TimeoutException</li>
 *   <li>INVALID_ARGUMENT - Données invalides → IllegalArgumentException</li>
 *   <li>INTERNAL - Erreur serveur → RuntimeException</li>
 * </ul>
 *
 * <p><strong>Performance :</strong></p>
 * <ul>
 *   <li>Réutilise le même canal gRPC (pool de connexions)</li>
 *   <li>Timeout configurables par appel (défaut: 5s)</li>
 *   <li>Compression gzip automatique si disponible</li>
 * </ul>
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
     * <p>
     * Cette méthode appelle le service gRPC {@code AuthService.GenerateTemporaryToken}
     * du module Auth. Le token temporaire généré permet à un membre nouvellement confirmé
     * de définir son mot de passe initial (expire en 15 minutes).
     * </p>
     *
     * <p><strong>Flow de communication :</strong></p>
     * <pre>
     * 1. Members: Utilisateur confirme son email
     * 2. Members → Auth (gRPC): GenerateTemporaryToken(email, memberId)
     * 3. Auth: Génère JWT temporaire
     * 4. Auth → Members (gRPC): TemporaryTokenResponse avec JWT
     * 5. Members → Utilisateur: Token dans réponse HTTP
     * </pre>
     *
     * @param email l'email du membre confirmé (requis, validé côté serveur)
     * @param memberId l'UUID du membre (requis)
     * @return le token temporaire JWT généré par le module Auth
     * @throws IllegalArgumentException si email ou memberId invalide
     * @throws AuthServiceUnavailableException si le service Auth est indisponible
     * @throws RuntimeException si erreur inattendue
     */
    @Override
    public String retrievePostActivationToken(String email, UUID memberId) {
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

            // Extraction du token de la réponse
            return response.getTemporaryToken();

        } catch (StatusRuntimeException e) {
            // Conversion des erreurs gRPC en exceptions métier
            throw handleGrpcException(e);
        }
    }

    // ========================================================================
    // Gestion des erreurs gRPC
    // ========================================================================

    /**
     * Convertit les exceptions gRPC en exceptions métier appropriées.
     * <p>
     * Cette méthode centralise la gestion des erreurs gRPC et les mappe
     * vers des exceptions Java plus expressives pour la couche métier.
     * </p>
     *
     * @param e l'exception gRPC interceptée
     * @return l'exception métier appropriée
     */
    private RuntimeException handleGrpcException(StatusRuntimeException e) {
        Status.Code code = e.getStatus().getCode();
        String description = e.getStatus().getDescription();

        return switch (code) {
            case UNAVAILABLE -> new AuthServiceUnavailableException(
                    "Auth service is unavailable: " + description, e);
            
            case DEADLINE_EXCEEDED -> new RuntimeException(
                    "Timeout exceeded while calling " + "generateTemporaryToken" + ": " + description, e);
            
            case INVALID_ARGUMENT -> new IllegalArgumentException(
                    "Invalid argument in " + "generateTemporaryToken" + ": " + description, e);
            
            case INTERNAL -> new RuntimeException(
                    "Internal error in Auth service during " + "generateTemporaryToken" + ": " + description, e);
            
            case UNAUTHENTICATED -> new SecurityException(
                    "Authentication failed during " + "generateTemporaryToken" + ": " + description, e);
            
            default -> new RuntimeException(
                    "Unexpected gRPC error during " + "generateTemporaryToken" + " [" + code + "]: " + description, e);
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

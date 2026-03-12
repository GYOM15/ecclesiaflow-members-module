package com.ecclesiaflow.web.client;

import com.ecclesiaflow.application.config.WebClientConfig;
import com.ecclesiaflow.business.domain.auth.AuthClient;
import com.ecclesiaflow.business.domain.auth.PasswordSetupTokenResponse;
import com.ecclesiaflow.web.model.TemporaryTokenRequest;
import com.ecclesiaflow.web.model.TemporaryTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implémentation REST (WebClient) du client Auth.
 * <p>
 * <strong>⚠️ LEGACY / FALLBACK ONLY</strong>
 * </p>
 * <p>
 * Cette implémentation est conservée pour :
 * <ul>
 *   <li>Fallback si gRPC a un problème en production</li>
 *   <li>Migration progressive (canary deployment)</li>
 *   <li>Tests de régression (comparaison REST vs gRPC)</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Production :</strong> Utiliser gRPC ({@link com.ecclesiaflow.io.grpc.client.AuthGrpcClient}) avec grpc.enabled=true
 * </p>
 * <p>
 * <strong>TODO (2025-06) :</strong> Supprimer après 6 mois de stabilité gRPC en production
 * </p>
 *
 * <p><strong>Rôle architectural :</strong> Service d'intégration - Communication inter-modules</p>
 *
 * @deprecated Préférer {@link com.ecclesiaflow.io.grpc.client.AuthGrpcClient} pour la communication inter-modules
 * @see com.ecclesiaflow.io.grpc.client.AuthGrpcClient
 *
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Génération de tokens temporaires après confirmation de compte</li>
 *   <li>Définition initiale du mot de passe avec token temporaire</li>
 *   <li>Changement de mot de passe pour utilisateurs authentifiés</li>
 *   <li>Gestion des erreurs de communication inter-modules</li>
 * </ul>
 *
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link WebClient} - Client HTTP réactif configuré pour le module d'auth</li>
 *   <li>Module d'authentification EcclesiaFlow (service externe)</li>
 * </ul>
 *
 * <p><strong>Configuration :</strong></p>
 * <ul>
 *   <li>ecclesiaflow.auth.module.enabled - Active/désactive l'intégration</li>
 *   <li>Configuration WebClient dans {@link WebClientConfig}</li>
 * </ul>
 *
 * <p><strong>Endpoints du module d'auth :</strong></p>
 * <ul>
 *   <li>POST /ecclesiaflow/auth/temporary-token - Génération de token temporaire</li>
 *   <li>POST /ecclesiaflow/auth/password - Définition initiale du mot de passe</li>
 *   <li>POST /ecclesiaflow/auth/new-password - Changement de mot de passe</li>
 * </ul>
 *
 * <p><strong>Gestion d'erreurs :</strong> Mode dégradé avec valeurs de test en cas
 * d'indisponibilité du module d'authentification.</p>
 *
 * <p><strong>Garanties :</strong> Asynchrone non-bloquant, resilient aux pannes, configurable.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see WebClient
 * @see WebClientConfig
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "false", matchIfMissing = true)
public class AuthClientImpl implements AuthClient {

    private final WebClient authWebClient;

    /**
     * Génère un token temporaire pour permettre la définition du mot de passe.
     * <p>
     * Appelle le module d'authentification pour générer un token temporaire
     * valide 1 heure, permettant à l'utilisateur de définir son mot de passe
     * après confirmation de son compte.
     * </p>
     *
     * @param email l'email du membre confirmé, non null
     * @return le token temporaire généré, ou une valeur de test en cas d'erreur
     *
     * @implNote En cas d'erreur de communication, retourne une valeur de test
     *           pour permettre le développement en mode dégradé.
     */
    @Override
    public PasswordSetupTokenResponse retrievePostActivationToken(String email, UUID memberId) {
        try {
            TemporaryTokenRequest request = new TemporaryTokenRequest(email, memberId);
            TemporaryTokenResponse response = post(request).block();
            return new PasswordSetupTokenResponse(
                    response.getTemporaryToken(),
                    response.getExpiresInSeconds() != null ? response.getExpiresInSeconds() : 86400,
                    response.getPasswordEndpoint() != null ? response.getPasswordEndpoint() : "/ecclesiaflow/auth/password/setup"
            );
        } catch (Exception e) {
            // Fallback for development
            return new PasswordSetupTokenResponse(
                    "temporary-token-mock-for-dev",
                    86400,
                    "/ecclesiaflow/auth/password/setup"
            );
        }
    }

    @Override
    public void deleteKeycloakUser(String keycloakUserId) {
        throw new UnsupportedOperationException(
                "deleteKeycloakUser requires gRPC (grpc.enabled=true)");
    }

    // === Méthodes utilitaires ===

    /**
     * Effectue un appel POST vers le module d'authentification avec réponse type-safe.
     * <p>
     * Méthode utilitaire pour les appels HTTP POST qui attendent une réponse
     * du module d'authentification. Gère les erreurs 4xx et 5xx automatiquement.
     * </p>
     *
     * @param body le corps de la requête (DTO)
     * @return un Mono contenant la réponse sous forme de DTO type-safe
     */
    private Mono<TemporaryTokenResponse> post(Object body) {
        return authWebClient
                .post()
                .uri("/ecclesiaflow/auth/temporary-token")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.createException().flatMap(Mono::error)
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.createException().flatMap(Mono::error)
                )
                .bodyToMono(TemporaryTokenResponse.class);
    }
}
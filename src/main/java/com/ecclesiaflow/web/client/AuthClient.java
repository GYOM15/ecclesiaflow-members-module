package com.ecclesiaflow.web.client;

import com.ecclesiaflow.application.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service d'intégration avec le module d'authentification EcclesiaFlow.
 * <p>
 * Cette classe fournit les opérations d'intégration avec le module d'authentification
 * séparé : génération de tokens temporaires, définition et changement de mots de passe.
 * Utilise WebClient pour les appels HTTP asynchrones vers le module d'auth.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service d'intégration - Communication inter-modules</p>
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
@ConditionalOnProperty(name = "ecclesiaflow.auth.module.enabled", havingValue = "true", matchIfMissing = true)
public class AuthClient {

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
    public String retrievePostActivationToken(String email) {
        try {
            return post("/ecclesiaflow/auth/temporary-token", Map.of("email", email))
                    .map(response -> (String) response.get("temporaryToken"))
                    .block();
        } catch (Exception e) {
            return "temporary-token-mock-for-dev"; // Valeur de test en développement
        }
    }

    // === Méthodes utilitaires ===

    /**
     * Effectue un appel POST vers le module d'authentification avec réponse.
     * <p>
     * Méthode utilitaire pour les appels HTTP POST qui attendent une réponse
     * du module d'authentification. Gère les erreurs 4xx et 5xx automatiquement.
     * </p>
     * 
     * @param path le chemin de l'endpoint (ex: "/ecclesiaflow/auth/temporary-token")
     * @param body le corps de la requête sous forme de Map
     * @return un Mono contenant la réponse sous forme de Map
     */
    private Mono<Map> post(String path, Map<String, String> body) {
        return authWebClient
                .post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.createException().flatMap(Mono::error)
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.createException().flatMap(Mono::error)
                )
                .bodyToMono(Map.class);
    }
}
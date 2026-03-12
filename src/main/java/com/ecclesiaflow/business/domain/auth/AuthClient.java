package com.ecclesiaflow.business.domain.auth;

import java.util.UUID;

/**
 * Service pour communiquer avec le module Auth d'EcclesiaFlow.
 * <p>
 * Ce service permet au module Members d'interroger le module Auth
 * pour générer des tokens temporaires lors de la confirmation de compte.
 * Respecte l'architecture inter-modules et la séparation des responsabilités.
 * </p>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Génération de tokens temporaires (post-confirmation)</li>
 *   <li>Communication inter-modules (REST ou gRPC)</li>
 *   <li>Gestion des erreurs de communication inter-modules</li>
 * </ul>
 * 
 * <p><strong>Implémentations :</strong></p>
 * <ul>
 *   <li>{@code AuthClientImpl} - Communication REST via WebClient</li>
 *   <li>{@code AuthGrpcClient} - Communication gRPC (si grpc.enabled=true)</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface AuthClient {
    
    /**
     * Génère un token temporaire pour permettre la définition du mot de passe.
     * <p>
     * Après confirmation de compte, cette méthode appelle le module Auth
     * pour générer un token temporaire (validité: 24 heures) permettant
     * à l'utilisateur de définir son mot de passe initial.
     * </p>
     * 
     * @param email l'email du membre confirmé
     * @param memberId l'identifiant UUID du membre
     * @return response containing token and password setup endpoint
     * @throws RuntimeException si la communication avec le module Auth échoue
     */
    PasswordSetupTokenResponse retrievePostActivationToken(String email, UUID memberId);

    /**
     * Supprime un utilisateur Keycloak via le module Auth.
     * <p>
     * Appelé avant la suppression d'un membre de la base de données
     * pour éviter les utilisateurs orphelins dans Keycloak.
     * </p>
     *
     * @param keycloakUserId l'identifiant Keycloak de l'utilisateur à supprimer
     * @throws RuntimeException si la communication avec le module Auth échoue
     */
    void deleteKeycloakUser(String keycloakUserId);
}

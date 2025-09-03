package com.ecclesiaflow.business.mappers;

import com.ecclesiaflow.business.domain.MembershipPassword;
import com.ecclesiaflow.web.dto.SetPasswordRequest;
import com.ecclesiaflow.web.dto.PasswordSetResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper pour les opérations de définition de mot de passe EcclesiaFlow.
 * <p>
 * Cette classe gère la transformation bidirectionnelle entre les DTOs web
 * et les objets métier pour les opérations de définition de mot de passe.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Mapper - Transformation DTOs ↔ Objets métier</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Transformation des requêtes web en objets métier {@link MembershipPassword}</li>
 *   <li>Génération de réponses standardisées pour les opérations de succès</li>
 *   <li>Centralisation de la logique de mapping pour la définition de mots de passe</li>
 *   <li>Isolation des détails de transformation entre les couches</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link SetPasswordRequest} - DTO d'entrée de la couche web</li>
 *   <li>{@link MembershipPassword} - Objet métier de la couche business</li>
 *   <li>{@link PasswordSetResponse} - DTO de réponse pour la couche web</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ol>
 *   <li>Réception d'une requête {@link SetPasswordRequest} du contrôleur</li>
 *   <li>Transformation en objet métier {@link MembershipPassword}</li>
 *   <li>Traitement par la couche service</li>
 *   <li>Génération d'une réponse de succès standardisée</li>
 * </ol>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (stateless), transformations cohérentes,
 * réponses standardisées, séparation des responsabilités.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MembershipPassword
 * @see SetPasswordRequest
 * @see PasswordSetResponse
 */
@Component
public class PasswordSetMapper {
    /**
     * Transforme une requête web de définition de mot de passe en objet métier.
     * <p>
     * Extrait les informations d'email et de mot de passe de la requête HTTP
     * et les encapsule dans un objet métier {@link MembershipPassword} pour
     * traitement par la couche service. Utilise le pattern Builder pour une
     * construction d'objet propre et lisible.
     * </p>
     * 
     * @param request la requête de définition de mot de passe, non null
     * @return un objet métier {@link MembershipPassword} contenant email et mot de passe
     * 
     * @implNote Utilise le pattern Builder de Lombok pour la construction d'objet.
     *           Aucune validation n'est effectuée ici, elle est déléguée aux couches appropriées.
     */
    public MembershipPassword fromSetPasswordRequest(SetPasswordRequest request) {
        return MembershipPassword.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }

    /**
     * Génère une réponse standardisée de succès pour la définition de mot de passe.
     * <p>
     * Crée un objet {@link PasswordSetResponse} avec un message de succès
     * et un statut standardisés. Cette méthode centralise la création des
     * réponses de succès pour maintenir la cohérence des messages utilisateur
     * à travers l'application.
     * </p>
     * 
     * @return une réponse de succès standardisée avec message et statut
     * 
     * @implNote Le message et le statut sont codés en dur pour garantir
     *           la cohérence. Pour l'internationalisation future, cette
     *           logique pourrait être externalisée dans un service de messages.
     */
    public PasswordSetResponse toSuccessResponse() {
        return PasswordSetResponse.builder()
                .message("Mot de passe défini avec succès")
                .status("SUCCESS")
                .build();
    }
}

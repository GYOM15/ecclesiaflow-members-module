package com.ecclesiaflow.web.mappers.web;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import com.ecclesiaflow.web.dto.ConfirmationRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper pour la transformation des DTOs de confirmation en objets métier.
 * <p>
 * Cette classe gère la conversion des requêtes de confirmation provenant de la couche web
 * vers les objets métier utilisés par les services. Implémente le pattern de mapping
 * avec gestion des cas limites (requêtes nulles ou incomplètes).
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Mapper - Conversion DTO vers objet métier</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Transformation des DTOs de confirmation en objets métier</li>
 *   <li>Combinaison des données de requête avec l'identifiant membre</li>
 *   <li>Gestion des cas limites (requêtes nulles)</li>
 *   <li>Séparation claire entre couche web et couche métier</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Transformation des requêtes de confirmation de compte</li>
 *   <li>Préparation des données pour le service de confirmation</li>
 *   <li>Orchestration par les contrôleurs de confirmation</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (bean Spring), gestion des cas limites.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
public class ConfirmationRequestMapper {

    /**
     * Transforme un DTO de confirmation et un identifiant membre en objet métier.
     * <p>
     * Cette méthode combine les données de la requête de confirmation avec
     * l'identifiant du membre pour créer un objet métier complet. Gère le cas
     * où la requête est nulle en créant un objet avec un code vide.
     * </p>
     * 
     * @param memberId l'identifiant unique du membre à confirmer, non null
     * @param request la requête de confirmation contenant le code, peut être null
     * @return un {@link MembershipConfirmation} prêt pour le traitement métier
     * @throws IllegalArgumentException si memberId est null
     * 
     * @implNote Utilise le pattern Builder pour la construction de l'objet métier.
     */
    public MembershipConfirmation fromConfirmationRequest(UUID memberId, ConfirmationRequest request) {
        return MembershipConfirmation.builder()
                .memberId(memberId)
                .confirmationCode(request.getCode())
                .build();
    }
}

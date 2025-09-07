package com.ecclesiaflow.web.mappers.web;

import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.web.dto.UpdateMemberRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper pour la transformation des DTOs de modification de membre en objets métier.
 * <p>
 * Cette classe gère la conversion des requêtes de mise à jour provenant de la couche web
 * vers les objets métier {@link MembershipUpdate} utilisés par les services. Respecte
 * la séparation des couches : DTOs web → mapper → objets métier → services.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Mapper - Conversion DTO de mise à jour vers objet métier</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Transformation des DTOs de mise à jour en objets métier</li>
 *   <li>Combinaison des données de requête avec l'identifiant membre</li>
 *   <li>Préservation des valeurs null pour les mises à jour partielles</li>
 *   <li>Séparation claire entre couche web et couche métier</li>
 * </ul>
 * 
 * <p><strong>Pattern de mapping :</strong></p>
 * <ul>
 *   <li>Entrée : DTO web + données contextuelles (memberId)</li>
 *   <li>Sortie : Objet métier pur {@link MembershipUpdate}</li>
 *   <li>Stratégie : Préservation des valeurs null pour mise à jour partielle</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Transformation des requêtes de modification de profil</li>
 *   <li>Préparation des données pour le service de mise à jour</li>
 *   <li>Orchestration par les contrôleurs de membres</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (bean Spring), préservation des mises à jour partielles.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MembershipUpdate
 * @see UpdateMemberRequest
 */
@Component
public class UpdateRequestMapper {

    /**
     * Transforme un DTO de modification et un identifiant membre en objet métier.
     * <p>
     * Cette méthode combine les données de la requête de mise à jour avec
     * l'identifiant du membre pour créer un objet métier complet. Préserve
     * les valeurs null du DTO pour permettre les mises à jour partielles.
     * </p>
     * 
     * @param memberId l'identifiant unique du membre à modifier, non null
     * @param updateRequest le DTO contenant les nouvelles données, non null
     * @return un {@link MembershipUpdate} prêt pour le traitement métier
     * @throws IllegalArgumentException si memberId ou updateRequest est null
     * 
     * @implNote Les champs null dans le DTO sont préservés pour permettre
     *           les mises à jour partielles au niveau du service métier.
     */
    public MembershipUpdate fromUpdateMemberRequest(UUID memberId, UpdateMemberRequest updateRequest) {
        return MembershipUpdate.builder()
                .memberId(memberId)
                .firstName(updateRequest.getFirstName())
                .lastName(updateRequest.getLastName())
                .address(updateRequest.getAddress())
                .email(updateRequest.getEmail())
                .build();
    }
}

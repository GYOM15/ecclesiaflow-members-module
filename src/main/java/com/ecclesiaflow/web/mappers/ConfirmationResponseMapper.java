package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.web.dto.ConfirmationResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper pour la transformation des résultats de confirmation métier en DTOs de réponse web.
 * <p>
 * Cette classe gère la conversion des objets métier {@link MembershipConfirmationResult}
 * vers les DTOs de réponse {@link ConfirmationResponse} utilisés par la couche web.
 * Respecte la séparation des couches : services métier → mapper → DTOs web.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Mapper - Conversion objet métier vers DTO de réponse</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Transformation des résultats de confirmation en DTOs web</li>
 *   <li>Préservation de l'intégrité des données lors de la conversion</li>
 *   <li>Séparation claire entre couche métier et couche web</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Transformation des réponses de confirmation réussie</li>
 *   <li>Préparation des données pour l'API REST</li>
 *   <li>Orchestration par les contrôleurs de confirmation</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (bean Spring), conversion fidèle des données.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MembershipConfirmationResult
 * @see ConfirmationResponse
 */
@Component
public class ConfirmationResponseMapper {

    /**
     * Transforme un résultat de confirmation métier en DTO de réponse web.
     * <p>
     * Cette méthode effectue une conversion directe des champs du résultat métier
     * vers le DTO de réponse, préservant toutes les informations nécessaires
     * pour la réponse API (message, token temporaire, durée d'expiration).
     * </p>
     * 
     * @param result le résultat de confirmation métier à convertir, non null
     * @return un {@link ConfirmationResponse} prêt pour la sérialisation JSON
     * @throws NullPointerException si result est null
     * 
     * @implNote Utilise le pattern Builder du DTO pour une construction claire.
     */
    public ConfirmationResponse fromMemberConfirmationResult(MembershipConfirmationResult result) {
        return ConfirmationResponse.builder()
                .message(result.getMessage())
                .temporaryToken(result.getTemporaryToken())
                .expiresIn(result.getExpiresInSeconds())
                .build();
    }
}

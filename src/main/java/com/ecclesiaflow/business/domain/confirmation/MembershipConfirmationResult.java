package com.ecclesiaflow.business.domain.confirmation;

import lombok.Builder;
import lombok.Data;
import com.ecclesiaflow.business.services.MemberConfirmationService;

/**
 * Objet métier représentant le résultat d'une confirmation de membre EcclesiaFlow.
 * <p>
 * Cette classe encapsule les données de réponse après une confirmation réussie
 * dans la couche métier. Respecte la séparation des couches en évitant l'utilisation
 * directe des DTOs de réponse web dans les services métier.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Objet métier - Résultat de confirmation des membres</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Encapsulation du résultat de confirmation réussie</li>
 *   <li>Transport du token temporaire pour définition du mot de passe</li>
 *   <li>Information sur l'expiration du token temporaire</li>
 *   <li>Séparation entre couche métier et couche web (DTOs)</li>
 * </ul>
 * 
 * <p><strong>Flux architectural :</strong></p>
 * <ol>
 *   <li>Service de confirmation → MembershipConfirmationResult</li>
 *   <li>MembershipConfirmationResult → Mapper → DTO de réponse (ConfirmationResponse)</li>
 *   <li>Pas de DTOs dans les services pour respecter la séparation des couches</li>
 * </ol>
 * 
 * <p><strong>Contenu typique :</strong></p>
 * <ul>
 *   <li>Message de succès pour l'utilisateur</li>
 *   <li>Token temporaire pour accéder à la définition du mot de passe</li>
 *   <li>Durée de validité du token (généralement 1 heure)</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, construction flexible, données cohérentes.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberConfirmationService
 * @see MembershipConfirmation
 */
@Data
@Builder
public class MembershipConfirmationResult {
    private String message;
    private String temporaryToken;
    private long expiresInSeconds;
}

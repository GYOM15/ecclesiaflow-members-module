package com.ecclesiaflow.business.domain.confirmation;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import com.ecclesiaflow.business.services.MemberConfirmationService;
import lombok.Getter;

/**
 * Objet métier représentant une demande de confirmation de membre EcclesiaFlow.
 * <p>
 * Cette classe encapsule les données nécessaires pour valider un code de confirmation
 * dans la couche métier. Respecte la séparation des couches en évitant l'utilisation
 * directe des DTOs web dans les services métier.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Objet métier - Modèle de confirmation des membres</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Encapsulation des données de validation de confirmation</li>
 *   <li>Liaison entre l'identifiant du membre et son code de confirmation</li>
 *   <li>Séparation entre couche web (DTOs) et couche métier</li>
 *   <li>Support de la validation métier des confirmations</li>
 * </ul>
 * 
 * <p><strong>Flux architectural :</strong></p>
 * <ol>
 *   <li>DTO web (ConfirmationRequest) → Mapper → MembershipConfirmation</li>
 *   <li>MembershipConfirmation → Service de confirmation → Validation et traitement</li>
 *   <li>Pas de DTOs dans les services pour respecter la séparation des couches</li>
 * </ol>
 * 
 * <p><strong>Cas d'utilisation :</strong></p>
 * <ul>
 *   <li>Confirmation initiale d'inscription avec code reçu par email</li>
 *   <li>Validation de codes de confirmation renvoyés</li>
 *   <li>Vérification de l'association membre-code avant activation</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, validation métier, construction flexible.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberConfirmationService
 * @see MembershipConfirmationResult
 */
@Getter
@Builder(toBuilder = true)
public class MembershipConfirmation {
    /**
     * Identifiant unique du membre à confirmer.
     * <p>
     * UUID du membre qui tente de confirmer son compte.
     * Doit correspondre à un membre existant dans la base de données
     * avec un statut de confirmation en attente.
     * </p>
     */
    private UUID memberId;
    
    /**
     * Code de confirmation à 6 chiffres saisi par le membre.
     * <p>
     * Code numérique que le membre a reçu par email et qu'il
     * saisit pour confirmer son adresse email. Doit correspondre
     * exactement au code stocké en base et ne pas avoir expiré.
     * </p>
     */
    private String confirmationCode;
}

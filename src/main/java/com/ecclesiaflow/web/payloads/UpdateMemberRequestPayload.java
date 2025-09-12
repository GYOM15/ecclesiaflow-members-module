package com.ecclesiaflow.web.payloads;

import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO représentant une requête de mise à jour des informations d'un membre EcclesiaFlow.
 * <p>
 * Cette classe encapsule les données modifiables d'un profil membre lors d'une
 * opération de mise à jour. Tous les champs sont optionnels, permettant des
 * mises à jour partielles selon les besoins de l'utilisateur.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> DTO web - Requête de mise à jour membre</p>
 * 
 * <p><strong>Stratégie de mise à jour :</strong></p>
 * <ul>
 *   <li>Mise à jour partielle : seuls les champs non-null sont modifiés</li>
 *   <li>Validation des tailles maximales pour chaque champ</li>
 *   <li>Conservation des valeurs existantes pour les champs omis</li>
 * </ul>
 * 
 * <p><strong>Champs modifiables :</strong></p>
 * <ul>
 *   <li>Nom de famille (max 50 caractères)</li>
 *   <li>Prénom (max 50 caractères)</li>
 *   <li>Adresse physique (max 200 caractères)</li>
 *   <li>Email (max 100 caractères)</li>
 * </ul>
 * 
 * <p><strong>Flux de traitement :</strong></p>
 * <ol>
 *   <li>Réception de la requête par le contrôleur</li>
 *   <li>Validation des contraintes de taille</li>
 *   <li>Conversion vers objet métier {@link MembershipUpdate}</li>
 *   <li>Traitement par le service métier</li>
 * </ol>
 * 
 * <p><strong>Sécurité :</strong> Validation côté serveur, pas de champs sensibles exposés.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MembershipUpdate
 * @see com.ecclesiaflow.web.controller.MembersController
 */
@Data
public class UpdateMemberRequestPayload {
    /**
     * Nouveau nom de famille du membre (optionnel).
     */
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String lastName;

    /**
     * Nouveau prénom du membre (optionnel).
     */
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;

    /**
     * Nouvelle adresse physique du membre (optionnelle).
     */
    @Size(min = 10, max = 200, message = "L'adresse doit contenir entre 10 et 200 caractères")
    private String address;

    /**
     * Nouvelle adresse email du membre (optionnelle).
     */
    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    /**
     * Nouveau numero du membre (optionnelle).
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Format de téléphone invalide")
    private String phoneNumber;
}
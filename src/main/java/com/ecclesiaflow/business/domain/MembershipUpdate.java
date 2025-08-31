package com.ecclesiaflow.business.domain;

import lombok.Builder;
import lombok.Data;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.io.entities.Member;

import java.util.UUID;

/**
 * Objet métier représentant une demande de modification d'un membre EcclesiaFlow.
 * <p>
 * Cette classe encapsule les données de mise à jour d'un profil membre existant.
 * Utilise le pattern Builder pour une construction flexible et supporte les
 * mises à jour partielles (champs optionnels null = pas de modification).
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Objet métier - Modèle de mise à jour des membres</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Encapsulation des données de modification de profil</li>
 *   <li>Support des mises à jour partielles (champs optionnels)</li>
 *   <li>Séparation entre couche web (DTOs) et couche métier</li>
 *   <li>Validation métier des modifications demandées</li>
 * </ul>
 * 
 * <p><strong>Flux architectural :</strong></p>
 * <ol>
 *   <li>DTO web (UpdateMemberRequest) → Mapper → MembershipUpdate</li>
 *   <li>MembershipUpdate → Service métier → Validation et persistance</li>
 *   <li>Pas de DTOs dans les services pour respecter la séparation des couches</li>
 * </ol>
 * 
 * <p><strong>Stratégie de mise à jour :</strong></p>
 * <ul>
 *   <li>Champs null = pas de modification (conserve la valeur existante)</li>
 *   <li>Champs non-null = nouvelle valeur à appliquer</li>
 *   <li>memberId obligatoire pour identifier le membre à modifier</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, validation métier, construction flexible.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Member
 * @see MemberService
 */
@Data
@Builder
public class MembershipUpdate {
    
    /**
     * Identifiant unique du membre à modifier.
     * <p>
     * UUID obligatoire pour identifier de manière unique le membre
     * dont le profil doit être mis à jour. Doit correspondre à un
     * membre existant et confirmé dans la base de données.
     * </p>
     */
    private UUID memberId;
    
    /**
     * Nouveau prénom du membre (optionnel).
     * <p>
     * Si non-null, remplace le prénom existant du membre.
     * Si null, le prénom actuel est conservé sans modification.
     * </p>
     */
    private String firstName;
    
    /**
     * Nouveau nom de famille du membre (optionnel).
     * <p>
     * Si non-null, remplace le nom de famille existant du membre.
     * Si null, le nom de famille actuel est conservé sans modification.
     * </p>
     */
    private String lastName;
    
    /**
     * Nouvelle adresse physique du membre (optionnel).
     * <p>
     * Si non-null, remplace l'adresse existante du membre.
     * Si null, l'adresse actuelle est conservée sans modification.
     * Peut être une chaîne vide pour effacer l'adresse existante.
     * </p>
     */
    private String address;
    
    /**
     * Nouvelle adresse email du membre (optionnel).
     * <p>
     * Si non-null, remplace l'email existant du membre.
     * Si null, l'email actuel est conservé sans modification.
     * <strong>Attention :</strong> La modification de l'email peut nécessiter
     * une nouvelle confirmation selon les règles métier.
     * </p>
     */
    private String email;
}

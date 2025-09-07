package com.ecclesiaflow.web.dto;

import com.ecclesiaflow.business.domain.member.Member;
import lombok.Builder;
import lombok.Data;

/**
 * DTO représentant la réponse contenant les informations d'un membre EcclesiaFlow.
 * <p>
 * Cette classe encapsule les données de profil d'un membre retournées par l'API
 * après inscription réussie ou consultation de profil. Respecte la séparation
 * des couches en servant d'interface entre les services métier et les clients.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> DTO web - Réponse de données membre</p>
 * 
 * <p><strong>Contenu de la réponse :</strong></p>
 * <ul>
 *   <li>Informations de profil (nom, prénom, email, adresse)</li>
 *   <li>Statut du compte (confirmé, actif, verrouillé)</li>
 *   <li>Informations d'authentification (rôle, token si applicable)</li>
 *   <li>Métadonnées temporelles (date de création)</li>
 * </ul>
 * 
 * <p><strong>Flux de génération :</strong></p>
 * <ol>
 *   <li>Service métier retourne entité {@link Member}</li>
 *   <li>Mapper convertit en SignUpResponse</li>
 *   <li>Contrôleur retourne la réponse au client</li>
 * </ol>
 * 
 * <p><strong>Cas d'utilisation :</strong></p>
 * <ul>
 *   <li>Réponse après inscription réussie</li>
 *   <li>Consultation de profil membre</li>
 *   <li>Mise à jour de profil</li>
 *   <li>Authentification et autorisation côté client</li>
 * </ul>
 * 
 * <p><strong>Sécurité :</strong> Ne contient jamais le mot de passe en clair,
 * seulement les informations publiques du profil.</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, construction flexible, données cohérentes.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Member
 * @see com.ecclesiaflow.web.controller.MembersController
 */
@Data
@Builder
public class SignUpResponse {
    /**
     * Message informatif pour l'utilisateur (optionnel).
     */
    private String message;
    
    /**
     * Adresse email du membre.
     */
    private String email;
    
    /**
     * Prénom du membre.
     */
    private String firstName;
    
    /**
     * Nom de famille du membre.
     */
    private String lastName;
    
    /**
     * Adresse physique du membre (optionnelle).
     */
    private String address;
    
    /**
     * Rôle du membre dans l'application.
     */
    private String role;

    /**
     * Token d'authentification (si applicable).
     */
    private String token;

    /**
     * Indique si le compte a été confirmé par email.
     */
    private boolean confirmed;
    
    /**
     * Date de création du compte (format ISO).
     */
    private String createdAt;

    /**
     * Date de confirmation du compte (format ISO).
     */
    private String confirmedAt;
}


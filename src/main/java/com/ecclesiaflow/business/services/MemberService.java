package com.ecclesiaflow.business.services;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
import com.ecclesiaflow.business.domain.member.SocialProvider;
import com.ecclesiaflow.business.exceptions.MemberNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service de domaine pour la gestion complète des membres EcclesiaFlow.
 * <p>
 * Cette interface définit l'ensemble des opérations de gestion des membres :
 * inscription, validation, mise à jour, consultation et suppression.
 * Respecte le principe de responsabilité unique (SRP) en centralisant
 * toute la logique métier liée aux membres.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service de domaine - Gestion des membres</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Inscription et validation des nouveaux membres</li>
 *   <li>Gestion du cycle de vie des comptes membres</li>
 *   <li>Vérification de l'unicité et du statut des emails</li>
 *   <li>Opérations CRUD sur les profils membres</li>
 *   <li>Validation des données métier</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>{@link MemberRepository} - Persistance des données</li>
 *   <li>Services de validation métier internes</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Inscription de nouveaux membres via formulaire web</li>
 *   <li>Consultation et mise à jour des profils existants</li>
 *   <li>Vérification du statut de confirmation des comptes</li>
 *   <li>Administration des membres par les pasteurs</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe (stateless), transactionnel, validation métier.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface MemberService {
    
    /**
     * Enregistre un nouveau membre dans le système.
     * <p>
     * Cette méthode valide les données d'inscription, encode le mot de passe
     * de manière sécurisée et persiste le nouveau membre en base de données.
     * Vérifie l'unicité de l'email avant l'enregistrement.
     * </p>
     *
     * @param registration les données d'enregistrement du membre, non null
     * @return le membre créé avec son identifiant généré
     * @throws IllegalArgumentException si l'email existe déjà ou si les données sont invalides
     * @implNote Opération transactionnelle en écriture.
     */
    Member registerMember(MembershipRegistration registration);

    /**
     * Vérifie si un email est déjà utilisé dans le système.
     * <p>
     * Cette méthode permet de valider l'unicité d'un email avant
     * l'inscription d'un nouveau membre. Utilisée pour prévenir
     * les doublons et assurer l'intégrité des données.
     * </p>
     * 
     * @param email l'adresse email à vérifier, non null
     * @return true si l'email existe déjà en base, false sinon
     * @throws IllegalArgumentException si email est null ou vide
     * 
     * @implNote Opération de lecture optimisée (index sur email).
     */
    boolean isEmailAlreadyUsed(String email);

    /**
     * Vérifie si un membre avec cet email est confirmé.
     * <p>
     * Cette méthode vérifie à la fois l'existence du membre et son
     * statut de confirmation. Utilisée par le module d'authentification
     * pour valider les tentatives de connexion.
     * </p>
     * 
     * @param email l'email du membre à vérifier, non null
     * @return true si le membre existe et est confirmé, false sinon
     * @throws IllegalArgumentException si email est null ou vide
     * @implNote Opération de lecture avec jointure sur statut de confirmation.
     */
    boolean isEmailConfirmed(String email);
    
    /**
     * Recherche un membre par son memberId (UUID partagé entre modules).
     * <p>
     * Le memberId est l'identifiant partagé entre le module auth et le module members.
     * C'est le claim 'cid' du JWT. Cette méthode est utilisée pour récupérer
     * un membre spécifique, que ce soit pour soi-même (scope:own) ou pour un autre (scope:all).
     * </p>
     * 
     * @param memberId l'UUID partagé du membre, non null
     * @return le membre correspondant, jamais null
     * @throws MemberNotFoundException si aucun membre n'existe avec ce memberId
     * @throws IllegalArgumentException si memberId est null
     * @implNote Opération de lecture par memberId (optimisée avec index unique).
     */
    Member findByMemberId(UUID memberId);
    
    /**
     * Récupère un membre par son keycloakUserId (sub claim du JWT Keycloak).
     * <p>
     * Le keycloakUserId est l'identifiant unique de l'utilisateur dans Keycloak.
     * Il correspond au claim 'sub' du JWT. Cette méthode est utilisée pour récupérer
     * le profil d'un utilisateur authentifié via Keycloak.
     * </p>
     * 
     * @param keycloakUserId l'identifiant Keycloak de l'utilisateur, non null
     * @return le membre correspondant, jamais null
     * @throws MemberNotFoundException si aucun membre n'existe avec ce keycloakUserId
     * @throws IllegalArgumentException si keycloakUserId est null ou vide
     * @implNote Opération de lecture par keycloakUserId (optimisée avec index unique).
     * @since 2.0.0
     */
    Member getByKeycloakUserId(String keycloakUserId);
    
    /**
     * Met à jour les informations d'un membre existant.
     * <p>
     * Cette méthode applique les modifications spécifiées au profil
     * d'un membre. Seuls les champs fournis sont mis à jour (patch partiel).
     * L'email et le statut de confirmation ne peuvent pas être modifiés.
     * </p>
     *
     * @param update les données de mise à jour, non null
     * @return le membre mis à jour avec les nouvelles informations
     * @throws MemberNotFoundException si le membre n'existe pas
     * @throws IllegalArgumentException                               si updateRequest est null ou invalide
     * @implNote Opération transactionnelle en écriture avec validation métier.
     */
    Member updateMember(MembershipUpdate update);
    
    /**
     * Soft-deletes a member: disables Keycloak login, sets status to DEACTIVATED.
     * Actual data anonymization happens after the grace period via scheduled cleanup.
     *
     * @param memberId the member's shared UUID
     * @throws MemberNotFoundException if no member exists with this memberId
     */
    void deactivateMember(UUID memberId);

    /**
     * Supprime définitivement un membre du système.
     * <p>
     * Cette méthode effectue une suppression complète du membre
     * et de toutes ses données associées. Opération irréversible
     * réservée aux administrateurs.
     * </p>
     *
     * @param id l'identifiant du membre à supprimer, non null
     * @throws MemberNotFoundException si le membre n'existe pas
     * @throws IllegalArgumentException si id est null
     *
     * @implNote Opération transactionnelle en écriture avec cascade sur données liées.
     */
    void deleteMember(UUID id);
    
    /**
     * Récupère une page de membres avec support de filtrage et recherche.
     * <p>
     * Cette méthode permet de récupérer les membres de manière paginée
     * avec des options de filtrage par statut et de recherche
     * par nom ou email. Optimisée pour les gros volumes de données.
     * </p>
     *
     * @param pageable les paramètres de pagination (page, taille, tri), non null
     * @param search terme de recherche optionnel pour filtrer par nom ou email
     * @param status filtre optionnel par statut (null = tous)
     * @return une page de membres correspondant aux critères
     * @throws IllegalArgumentException si pageable est null
     * @implNote Opération de lecture optimisée avec index sur email et nom
     * @since 1.0.0
     */
    Page<Member> getAllMembers(Pageable pageable, String search, com.ecclesiaflow.business.domain.member.MemberStatus status);

    /**
     * Registers a member via social login (Google/Facebook/Microsoft).
     * Skips email confirmation and creates the member directly as ACTIVE.
     *
     * @param keycloakUserId the Keycloak sub claim identifying the user
     * @param socialProvider the social identity provider used for authentication
     * @param registration   member profile data (pre-filled from JWT claims)
     * @return the newly created ACTIVE member
     * @throws com.ecclesiaflow.business.exceptions.SocialAccountAlreadyExistsException
     *         if a member with the same email or keycloakUserId already exists
     */
    Member registerSocialMember(String keycloakUserId, SocialProvider socialProvider,
                                MembershipRegistration registration);

    /**
     * Reactivates a DEACTIVATED member account during the grace period.
     * Sets status back to ACTIVE and clears the deactivation timestamp.
     *
     * @param memberId the member's shared UUID
     * @return the reactivated member
     * @throws MemberNotFoundException if no member exists with this memberId
     * @throws IllegalStateException if the member is not in DEACTIVATED status
     */
    Member reactivateMember(UUID memberId);
}

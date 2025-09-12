package com.ecclesiaflow.business.services;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import com.ecclesiaflow.business.domain.member.MembershipUpdate;
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
     * 
     * @implNote Opération de lecture avec jointure sur statut de confirmation.
     */
    boolean isEmailConfirmed(String email);
    
    /**
     * Recherche un membre par son identifiant unique.
     * <p>
     * Cette méthode récupère les informations complètes d'un membre
     * à partir de son UUID. Utilisée pour les opérations de consultation
     * et de mise à jour des profils.
     * </p>
     *
     * @param id l'identifiant unique du membre, non null
     * @return le membre correspondant avec toutes ses informations
     * @throws MemberNotFoundException si aucun membre avec cet ID n'existe
     * @throws IllegalArgumentException                               si id est null
     * @implNote Opération de lecture par clé primaire (optimisée).
     */
    Member findById(UUID id);
    
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
     * avec des options de filtrage par statut de confirmation et de recherche
     * par nom ou email. Optimisée pour les gros volumes de données.
     * </p>
     *
     * @param pageable les paramètres de pagination (page, taille, tri), non null
     * @param search terme de recherche optionnel pour filtrer par nom ou email
     * @param confirmed filtre optionnel par statut de confirmation (null = tous)
     * @return une page de membres correspondant aux critères
     * @throws IllegalArgumentException si pageable est null
     * @implNote Opération de lecture optimisée avec index sur email et nom
     * @since 1.0.0
     */
    Page<Member> getAllMembers(Pageable pageable, String search, Boolean confirmed);
}

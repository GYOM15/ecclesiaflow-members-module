package com.ecclesiaflow.business.services.repositories;

import com.ecclesiaflow.business.domain.MemberConfirmation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface du repository pour la gestion des confirmations de membres dans le domaine métier.
 * <p>
 * Cette interface définit le contrat pour la persistance des confirmations de membres
 * du point de vue du domaine métier. Elle ne dépend d'aucune technologie de persistance
 * spécifique et utilise uniquement les objets du domaine.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Interface du domaine - Contrat de persistance</p>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Définir les opérations de persistance pour les confirmations</li>
 *   <li>Fournir des méthodes de recherche métier</li>
 *   <li>Maintenir l'indépendance du domaine vis-à-vis de la persistance</li>
 *   <li>Supporter les cas d'usage de confirmation des comptes</li>
 * </ul>
 * 
 * <p><strong>Opérations supportées :</strong></p>
 * <ul>
 *   <li>Recherche de confirmations par membre</li>
 *   <li>Recherche de confirmations par code</li>
 *   <li>Gestion du cycle de vie des confirmations</li>
 *   <li>Nettoyage des confirmations expirées</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Interface pure du domaine, indépendante de JPA,
 * contrat stable pour les services métier.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface MemberConfirmationRepository {

    /**
     * Recherche le code de confirmation actif pour un membre donné.
     * <p>
     * Cette méthode retourne le code de confirmation le plus récent
     * associé à un membre, qu'il soit expiré ou non. Utilisée pour
     * vérifier l'existence d'un processus de confirmation en cours.
     * </p>
     *
     * @param memberId l'identifiant du membre, non null
     * @return un {@link Optional} contenant le code de confirmation si trouvé, vide sinon
     * @throws IllegalArgumentException si memberId est null
     */
    Optional<MemberConfirmation> findByMemberId(UUID memberId);

    /**
     * Recherche un code de confirmation spécifique pour un membre donné.
     * <p>
     * Cette méthode valide qu'un code appartient bien au membre spécifié.
     * Utilisée lors de la validation des tentatives de confirmation pour
     * éviter les attaques par force brute inter-comptes.
     * </p>
     *
     * @param memberId l'identifiant du membre, non null
     * @param code le code de confirmation à 6 chiffres, non null
     * @return un {@link Optional} contenant le code si trouvé et valide, vide sinon
     * @throws IllegalArgumentException si un paramètre est null
     */
    Optional<MemberConfirmation> findByMemberIdAndCode(UUID memberId, String code);

    /**
     * Recherche une confirmation par son code.
     * <p>
     * Permet de retrouver une confirmation à partir du code saisi par l'utilisateur.
     * Utilisé lors du processus de validation du code de confirmation.
     * </p>
     *
     * @param code le code de confirmation, non null
     * @return un Optional contenant la confirmation si le code existe
     */
    Optional<MemberConfirmation> findByCode(String code);

    /**
     * Vérifie l'existence d'une confirmation pour un membre.
     *
     * @param memberId l'identifiant du membre, non null
     * @return true si une confirmation existe pour ce membre
     */
    boolean existsByMemberId(UUID memberId);

    /**
     * Recherche toutes les confirmations expirées.
     * <p>
     * Utile pour les tâches de nettoyage automatique des confirmations
     * qui ont dépassé leur date d'expiration.
     * </p>
     *
     * @return la liste des confirmations expirées
     */
    List<MemberConfirmation> findExpiredConfirmations();

    /**
     * Compte le nombre de confirmations en attente.
     * <p>
     * Statistique utile pour le monitoring et les tableaux de bord
     * administratifs.
     * </p>
     *
     * @return le nombre de confirmations non expirées
     */
    long countPendingConfirmations();

    /**
     * Sauvegarde une confirmation.
     * <p>
     * Crée une nouvelle confirmation ou met à jour une confirmation existante
     * selon la présence de l'identifiant.
     * </p>
     *
     * @param confirmation la confirmation à sauvegarder, non null
     * @return la confirmation sauvegardée avec son identifiant généré
     */
    MemberConfirmation save(MemberConfirmation confirmation);

    /**
     * Supprime une confirmation.
     *
     * @param confirmation la confirmation à supprimer, non null
     */
    void delete(MemberConfirmation confirmation);

    /**
     * Supprime toutes les confirmations expirées.
     * <p>
     * Opération de nettoyage pour maintenir la base de données propre.
     * Généralement appelée par une tâche planifiée.
     * </p>
     *
     * @return le nombre de confirmations supprimées
     */
    int deleteExpiredConfirmations();
}

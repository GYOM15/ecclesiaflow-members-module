package com.ecclesiaflow.io.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour la gestion des entités MemberConfirmationEntity.
 * <p>
 * Cette interface fournit les opérations CRUD et de recherche pour les confirmations
 * de membres au niveau de la couche de persistance. Elle travaille exclusivement
 * avec les entités JPA et ne connaît rien du domaine métier.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Repository JPA - Accès aux données</p>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Opérations CRUD sur les entités MemberConfirmationEntity</li>
 *   <li>Requêtes de recherche optimisées</li>
 *   <li>Gestion des requêtes personnalisées avec @Query</li>
 *   <li>Support des opérations de nettoyage en lot</li>
 * </ul>
 * 
 * <p><strong>Optimisations :</strong></p>
 * <ul>
 *   <li>Requêtes natives pour les opérations de nettoyage</li>
 *   <li>Index sur memberId et code pour les performances</li>
 *   <li>Requêtes avec paramètres pour éviter les injections SQL</li>
 * </ul>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Repository
public interface SpringDataMemberConfirmationRepository extends JpaRepository<MemberConfirmationEntity, UUID> {

    /**
     * Recherche une confirmation par l'identifiant du membre.
     * 
     * @param memberId l'identifiant du membre
     * @return un Optional contenant la confirmation si trouvée
     */
    Optional<MemberConfirmationEntity> findByMemberId(UUID memberId);

    /**
     * Recherche une confirmation par l'identifiant du membre et le code.
     * <p>
     * Cette méthode permet de valider qu'un code appartient bien au membre spécifié.
     * Utile pour éviter les attaques par force brute inter-comptes.
     * </p>
     * 
     * @param memberId l'identifiant du membre
     * @param code le code de confirmation
     * @return un Optional contenant la confirmation si trouvée
     */
    Optional<MemberConfirmationEntity> findByMemberIdAndCode(UUID memberId, String code);

    /**
     * Recherche une confirmation par son code.
     * 
     * @param code le code de confirmation
     * @return un Optional contenant la confirmation si trouvée
     */
    Optional<MemberConfirmationEntity> findByCode(String code);

    /**
     * Vérifie l'existence d'une confirmation pour un membre.
     * 
     * @param memberId l'identifiant du membre
     * @return true si une confirmation existe
     */
    boolean existsByMemberId(UUID memberId);

    /**
     * Recherche toutes les confirmations expirées.
     * <p>
     * Utilise une requête personnalisée pour identifier les confirmations
     * dont la date d'expiration est antérieure à l'heure actuelle.
     * </p>
     * 
     * @param now l'heure actuelle pour la comparaison
     * @return la liste des confirmations expirées
     */
    @Query("SELECT c FROM MemberConfirmationEntity c WHERE c.expiresAt < :now")
    List<MemberConfirmationEntity> findExpiredConfirmations(@Param("now") LocalDateTime now);

    /**
     * Compte le nombre de confirmations non expirées.
     * 
     * @param now l'heure actuelle pour la comparaison
     * @return le nombre de confirmations en attente
     */
    @Query("SELECT COUNT(c) FROM MemberConfirmationEntity c WHERE c.expiresAt >= :now")
    long countPendingConfirmations(@Param("now") LocalDateTime now);

    /**
     * Supprime toutes les confirmations expirées.
     * <p>
     * Opération de nettoyage en lot pour maintenir la base de données propre.
     * Utilise @Modifying pour indiquer qu'il s'agit d'une opération de modification.
     * </p>
     * 
     * @param now l'heure actuelle pour la comparaison
     * @return le nombre de confirmations supprimées
     */
    @Modifying
    @Query("DELETE FROM MemberConfirmationEntity c WHERE c.expiresAt < :now")
    int deleteExpiredConfirmations(@Param("now") LocalDateTime now);

    /**
     * Supprime toutes les confirmations d'un membre spécifique.
     * <p>
     * Utile lors de la suppression d'un membre pour maintenir
     * l'intégrité référentielle.
     * </p>
     * 
     * @param memberId l'identifiant du membre
     * @return le nombre de confirmations supprimées
     */
    @Modifying
    @Query("DELETE FROM MemberConfirmationEntity c WHERE c.memberId = :memberId")
    int deleteByMemberId(@Param("memberId") UUID memberId);
}

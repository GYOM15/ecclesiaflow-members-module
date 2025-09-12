package com.ecclesiaflow.io.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour la gestion des entités MemberEntity.
 * <p>
 * Cette interface fournit les opérations CRUD et de recherche pour les membres
 * au niveau de la couche de persistance. Elle travaille exclusivement avec les
 * entités JPA {@link MemberEntity} et ne connaît rien du domaine métier.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Repository JPA - Accès direct aux données</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Opérations CRUD automatiques via JpaRepository</li>
 *   <li>Requêtes de recherche personnalisées par méthodes nommées</li>
 *   <li>Opérations de comptage optimisées</li>
 *   <li>Gestion des contraintes d'unicité (email)</li>
 * </ul>
 * 
 * <p><strong>Avantages Spring Data JPA :</strong></p>
 * <ul>
 *   <li>Génération automatique des implémentations</li>
 *   <li>Requêtes dérivées des noms de méthodes</li>
 *   <li>Optimisations automatiques (exists, count)</li>
 *   <li>Support transactionnel intégré</li>
 * </ul>
 * 
 * <p><strong>Utilisation :</strong> Exclusivement par {@link com.ecclesiaflow.io.persistence.repositories.impl.MemberRepositoryImpl}
 * pour l'adaptation vers la couche domaine.</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, optimisé par Spring Data.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberEntity
 * @see com.ecclesiaflow.io.persistence.repositories.impl.MemberRepositoryImpl
 */
@Repository
public interface SpringDataMemberRepository extends JpaRepository<MemberEntity, UUID> {
    
    /**
     * Recherche une entité membre par son adresse email.
     * <p>
     * Requête dérivée Spring Data : SELECT * FROM member WHERE email = ?
     * Utilise l'index unique sur la colonne email pour des performances optimales.
     * </p>
     * 
     * @param email l'adresse email à rechercher, non null
     * @return un Optional contenant l'entité si trouvée, vide sinon
     */
    Optional<MemberEntity> findByEmail(String email);

    /**
     * Vérifie l'existence d'un membre avec l'email spécifié.
     * <p>
     * Requête optimisée Spring Data : SELECT EXISTS(SELECT 1 FROM member WHERE email = ?)
     * Plus efficace que findByEmail().isPresent() car ne charge pas l'entité complète.
     * </p>
     * 
     * @param email l'adresse email à vérifier, non null
     * @return true si un membre existe avec cet email, false sinon
     */
    boolean existsByEmail(String email);

    /**
     * Recherche toutes les entités membres par statut de confirmation.
     * <p>
     * Requête dérivée : SELECT * FROM member WHERE confirmed = ?
     * Utilise l'index sur la colonne confirmed pour les performances.
     * </p>
     * 
     * @param confirmed true pour les membres confirmés, false pour ceux en attente
     * @param pageable les paramètres de pagination et de tri, non null
     * @return la liste des entités correspondantes (peut être vide)
     */
    Page<MemberEntity> findByConfirmed(Boolean confirmed, Pageable pageable);

    /**
     * Compte le nombre de membres confirmés.
     * <p>
     * Requête optimisée : SELECT COUNT(*) FROM member WHERE confirmed = true
     * Opération de comptage pure sans chargement des entités.
     * </p>
     * 
     * @return le nombre de membres confirmés (≥ 0)
     */
    long countByConfirmedTrue();

    /**
     * Compte le nombre de membres en attente de confirmation.
     * <p>
     * Requête optimisée : SELECT COUNT(*) FROM member WHERE confirmed = false
     * Opération de comptage pure sans chargement des entités.
     * </p>
     * 
     * @return le nombre de membres non confirmés (≥ 0)
     */
    long countByConfirmedFalse();

    /**
     * Recherche des membres par prénom, nom ou email (insensible à la casse) avec pagination.
     * Les termes de recherche sont appliqués avec un opérateur LIKE.
     *
     * @param searchTerm Terme de recherche appliqué au prénom, nom et email. Peut être null ou vide.
     * @param pageable les paramètres de pagination et de tri.
     * @return une page d'entités membres correspondant aux critères.
     */
    @Query("SELECT m FROM MemberEntity m " +
           "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
           " LOWER(m.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(m.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(m.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<MemberEntity> findMembersBySearchTerm(
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    /**
     * Recherche des membres par prénom, nom ou email (insensible à la casse) et par statut de confirmation avec pagination.
     * Les termes de recherche sont appliqués avec un opérateur LIKE.
     *
     * @param searchTerm Terme de recherche appliqué au prénom, nom et email. Peut être null ou vide.
     * @param confirmed Le statut de confirmation recherché (true/false). Peut être null pour ignorer le filtre.
     * @param pageable les paramètres de pagination et de tri.
     * @return une page d'entités membres correspondant aux critères.
     */
    @Query("SELECT m FROM MemberEntity m " +
           "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
           " LOWER(m.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(m.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(m.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:confirmed IS NULL OR m.confirmed = :confirmed)")
    Page<MemberEntity> findMembersBySearchTermAndConfirmationStatus(
        @Param("searchTerm") String searchTerm,
        @Param("confirmed") Boolean confirmed,
        Pageable pageable
    );
}

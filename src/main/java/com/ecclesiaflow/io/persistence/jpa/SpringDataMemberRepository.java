package com.ecclesiaflow.io.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecclesiaflow.business.domain.member.MemberStatus;
import java.time.LocalDateTime;
import java.util.List;
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
     * Recherche une entité membre par son memberId (UUID du module auth).
     * <p>
     * Requête dérivée Spring Data : SELECT * FROM member WHERE member_id = ?
     * Utilise l'index unique sur la colonne member_id pour des performances optimales.
     * </p>
     * 
     * @param memberId l'UUID du membre (claim 'cid' du JWT), non null
     * @return un Optional contenant l'entité si trouvée, vide sinon
     */
    Optional<MemberEntity> findByMemberId(UUID memberId);

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
     * Recherche une entité membre par son keycloakUserId (sub claim du JWT Keycloak).
     * <p>
     * Requête dérivée Spring Data : SELECT * FROM member WHERE keycloak_user_id = ?
     * Utilise l'index unique sur la colonne keycloak_user_id pour des performances optimales.
     * </p>
     * 
     * @param keycloakUserId l'identifiant Keycloak de l'utilisateur, non null
     * @return un Optional contenant l'entité si trouvée, vide sinon
     */
    Optional<MemberEntity> findByKeycloakUserId(String keycloakUserId);

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

    boolean existsByKeycloakUserId(String keycloakUserId);

    /**
     * Recherche toutes les entités membres par statut.
     * <p>
     * Requête dérivée : SELECT * FROM member WHERE status = ?
     * Utilise l'index sur la colonne status pour les performances.
     * </p>
     * 
     * @param status le statut recherché (PENDING, CONFIRMED, ACTIVE, etc.)
     * @param pageable les paramètres de pagination et de tri, non null
     * @return la liste des entités correspondantes (peut être vide)
     */
    Page<MemberEntity> findByStatus(com.ecclesiaflow.business.domain.member.MemberStatus status, Pageable pageable);

    /**
     * Compte le nombre de membres actifs.
     * <p>
     * Requête optimisée : SELECT COUNT(*) FROM member WHERE status = 'ACTIVE'
     * Opération de comptage pure sans chargement des entités.
     * </p>
     * 
     * @return le nombre de membres actifs (≥ 0)
     */
    long countByStatus(com.ecclesiaflow.business.domain.member.MemberStatus status);


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
     * Recherche des membres par prénom, nom ou email (insensible à la casse) et par statut avec pagination.
     * Les termes de recherche sont appliqués avec un opérateur LIKE.
     *
     * @param searchTerm Terme de recherche appliqué au prénom, nom et email. Peut être null ou vide.
     * @param status Le statut recherché (PENDING, CONFIRMED, ACTIVE, etc.). Peut être null pour ignorer le filtre.
     * @param pageable les paramètres de pagination et de tri.
     * @return une page d'entités membres correspondant aux critères.
     */
    @Query("SELECT m FROM MemberEntity m " +
           "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
           " LOWER(m.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(m.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(m.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:status IS NULL OR m.status = :status)")
    Page<MemberEntity> findMembersBySearchTermAndStatus(
        @Param("searchTerm") String searchTerm,
        @Param("status") com.ecclesiaflow.business.domain.member.MemberStatus status,
        Pageable pageable
    );

    List<MemberEntity> findByStatusAndDeactivatedAtBefore(MemberStatus status, LocalDateTime cutoff);
}

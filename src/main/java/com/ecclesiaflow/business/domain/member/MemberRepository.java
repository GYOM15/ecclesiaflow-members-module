package com.ecclesiaflow.business.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository de domaine pour la gestion des membres EcclesiaFlow.
 * <p>
 * Cette interface définit le contrat d'accès aux données pour les objets métier {@link Member}.
 * Elle fait partie de la couche domaine et ne connaît rien des détails de persistance (JPA, SQL, etc.).
 * L'implémentation concrète se charge de l'adaptation vers la couche de persistance.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Repository de domaine - Contrat d'accès aux données</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Définition des opérations CRUD pour les objets {@link Member}</li>
 *   <li>Requêtes métier spécialisées (recherche par email, statut de confirmation)</li>
 *   <li>Opérations de comptage pour les statistiques</li>
 *   <li>Isolation du domaine des détails de persistance</li>
 * </ul>
 * 
 * <p><strong>Avantages du pattern Repository :</strong></p>
 * <ul>
 *   <li>Séparation claire entre logique métier et persistance</li>
 *   <li>Testabilité avec mocks et stubs</li>
 *   <li>Flexibilité pour changer la technologie de persistance</li>
 *   <li>Respect des principes DDD (Domain-Driven Design)</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Recherche de membres par email pour l'authentification</li>
 *   <li>Vérification d'unicité des emails lors de l'inscription</li>
 *   <li>Filtrage des membres par statut de confirmation</li>
 *   <li>Génération de statistiques sur les confirmations</li>
 * </ul>
 * 
 * <p><strong>Implémentation :</strong> {@link com.ecclesiaflow.io.persistence.repositories.impl.MemberRepositoryImpl}</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel (délégué à l'implémentation),
 * manipulation d'objets domaine purs.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Member
 * @see com.ecclesiaflow.io.persistence.repositories.impl.MemberRepositoryImpl
 */
public interface MemberRepository {

    /**
     * Recherche un membre par son identifiant unique.
     * 
     * @param id l'identifiant unique du membre, non null
     * @return un Optional contenant le membre si trouvé, vide sinon
     * @throws IllegalArgumentException si id est null
     */
    Optional<Member> findById(UUID id);
    
    /**
     * Recherche un membre par son adresse email.
     * <p>
     * Utilisé principalement pour l'authentification et la vérification d'unicité.
     * </p>
     * 
     * @param email l'adresse email du membre, non null et non vide
     * @return un Optional contenant le membre si trouvé, vide sinon
     * @throws IllegalArgumentException si email est null ou vide
     */
    Optional<Member> findByEmail(String email);
    
    /**
     * Vérifie l'existence d'un membre avec l'email spécifié.
     * <p>
     * Méthode optimisée pour la vérification d'unicité lors de l'inscription.
     * Plus efficace que findByEmail().isPresent().
     * </p>
     * 
     * @param email l'adresse email à vérifier, non null et non vide
     * @return true si un membre existe avec cet email, false sinon
     * @throws IllegalArgumentException si email est null ou vide
     */
    boolean existsByEmail(String email);
    
    /**
     * Récupère tous les membres du système.
     * <p>
     * À utiliser avec précaution sur de gros volumes de données.
     * Considérer la pagination pour les interfaces utilisateur.
     * </p>
     * 
     * @return la liste de tous les membres (peut être vide)
     */
    List<Member> findAll();
    
    /**
     * Compte le nombre de membres ayant confirmé leur compte.
     * 
     * @return le nombre de membres confirmés (≥ 0)
     */
    long countConfirmedMembers();
    
    /**
     * Compte le nombre de membres en attente de confirmation.
     * 
     * @return le nombre de membres non confirmés (≥ 0)
     */
    long countPendingConfirmations();
    
    /**
     * Sauvegarde un membre (création ou mise à jour).
     * <p>
     * Si le membre n'existe pas (id null), il sera créé.
     * Si le membre existe, il sera mis à jour.
     * </p>
     * 
     * @param member le membre à sauvegarder, non null
     * @return le membre sauvegardé avec les données actualisées
     * @throws IllegalArgumentException si member est null
     */
    Member save(Member member);
    
    /**
     * Supprime un membre du système.
     * <p>
     * Attention : cette opération est irréversible et peut affecter
     * l'intégrité référentielle avec d'autres modules.
     * </p>
     * 
     * @param member le membre à supprimer, non null
     * @throws IllegalArgumentException si member est null
     * @throws IllegalStateException si le membre n'existe pas
     */
    void delete(Member member);
    
    /**
     * Récupère une page de tous les membres du système.
     * <p>
     * Version paginée de findAll() pour optimiser les performances
     * sur de gros volumes de données.
     * </p>
     * 
     * @param pageable les paramètres de pagination, non null
     * @return une page de membres
     * @throws IllegalArgumentException si pageable est null
     */
    Page<Member> getAll(Pageable pageable);
    
    /**
     * Recherche des membres par statut de confirmation avec pagination.
     * 
     * @param confirmed le statut de confirmation recherché
     * @param pageable les paramètres de pagination, non null
     * @return une page de membres correspondant au critère
     * @throws IllegalArgumentException si pageable est null
     */
    Page<Member> getByConfirmedStatus(Boolean confirmed, Pageable pageable);
    
    /**
     * Recherche des membres par prénom, nom ou email (insensible à la casse) avec pagination.
     * Les termes de recherche sont appliqués avec un opérateur LIKE.
     *
     * @param searchTerm Terme de recherche appliqué au prénom, nom et email. Peut être null ou vide.
     * @param pageable les paramètres de pagination et de tri, non null
     * @return une page de membres correspondant aux critères
     * @throws IllegalArgumentException si pageable est null
     */
    Page<Member> getMembersBySearchTerm(String searchTerm, Pageable pageable);
    
    /**
     * Recherche des membres par prénom, nom ou email (insensible à la casse) et par statut de confirmation avec pagination.
     * Les termes de recherche sont appliqués avec un opérateur LIKE.
     *
     * @param searchTerm Terme de recherche appliqué au prénom, nom et email. Peut être null ou vide.
     * @param confirmed Le statut de confirmation recherché (true/false). Peut être null pour ignorer le filtre.
     * @param pageable les paramètres de pagination et de tri, non null
     * @return une page de membres correspondant aux critères
     * @throws IllegalArgumentException si pageable est null
     */
    Page<Member> getMembersBySearchTermAndConfirmationStatus(
        String searchTerm, Boolean confirmed, Pageable pageable);
}

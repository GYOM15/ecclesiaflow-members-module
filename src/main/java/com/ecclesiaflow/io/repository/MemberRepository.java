package com.ecclesiaflow.io.repository;

import com.ecclesiaflow.io.entities.Member;
import com.ecclesiaflow.business.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour la gestion des entités Member.
 * <p>
 * Cette interface définit les opérations de persistance pour les membres EcclesiaFlow.
 * Étend {@link JpaRepository} pour bénéficier des opérations CRUD standard et
 * ajoute des méthodes de recherche spécifiques au domaine métier.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Couche d'accès aux données (Repository Pattern)</p>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>Spring Data JPA - Génération automatique des implémentations</li>
 *   <li>Base de données relationnelle - Stockage persistant</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Recherche de membre par email lors de l'authentification</li>
 *   <li>Recherche de membre par rôle pour les autorisations</li>
 *   <li>Opérations CRUD standard via JpaRepository</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, gestion automatique des connexions.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface MemberRepository extends JpaRepository<Member, UUID> {

    /**
     * Recherche un membre par son adresse email.
     * <p>
     * L'email étant unique dans le système, cette méthode retourne au maximum
     * un seul membre. Utilisée principalement pour l'authentification.
     * </p>
     * 
     * @param email l'adresse email du membre à rechercher, non null
     * @return un {@link Optional} contenant le membre si trouvé, vide sinon
     * @throws IllegalArgumentException si email est null
     * 
     * @implNote Génère automatiquement la requête SQL : SELECT * FROM members WHERE email = ?
     */
    Optional<Member> findByEmail(String email);

    /**
     * Recherche un membre par son rôle.
     * <p>
     * Cette méthode retourne le premier membre trouvé avec le rôle spécifié.
     * Utilisée pour les opérations d'administration et de gestion des permissions.
     * </p>
     * 
     * @param role le rôle du membre à rechercher, non null
     * @return le membre ayant ce rôle, null si aucun membre trouvé
     * @throws IllegalArgumentException si role est null
     * 
     * @implNote Génère automatiquement la requête SQL : SELECT * FROM members WHERE role = ? LIMIT 1
     */
    Member findByRole(Role role);


    /**
     * Vérifie l'existence d'un membre par son adresse email.
     * <p>
     * Cette méthode optimisée vérifie uniquement l'existence sans charger
     * l'entité complète. Plus performante que findByEmail() pour les vérifications
     * d'unicité lors de l'inscription.
     * </p>
     * 
     * @param email l'adresse email à vérifier, non null
     * @return true si un membre avec cet email existe, false sinon
     * @throws IllegalArgumentException si email est null
     * 
     * @implNote Génère une requête COUNT optimisée : SELECT COUNT(*) FROM members WHERE email = ?
     */
    boolean existsByEmail(String email);

    /**
     * Recherche tous les membres selon leur statut de confirmation.
     * <p>
     * Cette méthode permet de filtrer les membres confirmés ou en attente
     * de confirmation. Utilisée pour les rapports administratifs et
     * les opérations de maintenance.
     * </p>
     * 
     * @param confirmed true pour les membres confirmés, false pour ceux en attente
     * @return la liste des membres correspondant au statut, peut être vide
     * 
     * @implNote Requête JPQL avec paramètre : SELECT m FROM Member m WHERE m.confirmed = :confirmed
     */
    @Query("SELECT m FROM Member m WHERE m.confirmed = :confirmed")
    List<Member> findByConfirmedStatus(@Param("confirmed") boolean confirmed);

    /**
     * Compte le nombre de membres confirmés dans le système.
     * <p>
     * Cette méthode de comptage optimisée retourne le nombre total
     * de membres ayant confirmé leur compte. Utilisée pour les
     * statistiques et tableaux de bord administratifs.
     * </p>
     * 
     * @return le nombre de membres confirmés (≥ 0)
     * 
     * @implNote Requête COUNT optimisée : SELECT COUNT(m) FROM Member m WHERE m.confirmed = true
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.confirmed = true")
    long countConfirmedMembers();

    /**
     * Compte le nombre de membres en attente de confirmation.
     * <p>
     * Cette méthode de comptage optimisée retourne le nombre total
     * de membres n'ayant pas encore confirmé leur compte. Utilisée
     * pour le suivi des inscriptions en cours et les relances.
     * </p>
     * 
     * @return le nombre de membres en attente de confirmation (≥ 0)
     * 
     * @implNote Requête COUNT optimisée : SELECT COUNT(m) FROM Member m WHERE m.confirmed = false
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.confirmed = false")
    long countPendingConfirmations();
}

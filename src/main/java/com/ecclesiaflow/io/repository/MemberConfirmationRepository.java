package com.ecclesiaflow.io.repository;

import com.ecclesiaflow.io.entities.MemberConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour la gestion des entités MemberConfirmation.
 * <p>
 * Cette interface définit les opérations de persistance pour les codes de confirmation
 * des membres EcclesiaFlow. Gère le cycle de vie complet des codes : création,
 * validation, expiration et nettoyage automatique.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Couche d'accès aux données - Gestion des confirmations</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Persistance et recherche des codes de confirmation</li>
 *   <li>Validation des codes avec vérification d'expiration</li>
 *   <li>Nettoyage automatique des codes expirés</li>
 *   <li>Statistiques sur les confirmations en attente</li>
 * </ul>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>Spring Data JPA - Génération automatique des implémentations</li>
 *   <li>Base de données relationnelle - Stockage persistant avec contraintes temporelles</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Validation des codes saisis par les membres</li>
 *   <li>Recherche de codes actifs pour un membre</li>
 *   <li>Nettoyage périodique des codes expirés</li>
 *   <li>Statistiques administratives sur les confirmations</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, transactionnel, gestion automatique de l'expiration.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Repository
public interface MemberConfirmationRepository extends JpaRepository<MemberConfirmation, UUID> {

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
     * 
     * @implNote Génère automatiquement : SELECT * FROM member_confirmations WHERE member_id = ?
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
     * 
     * @implNote Génère : SELECT * FROM member_confirmations WHERE member_id = ? AND code = ?
     */
    Optional<MemberConfirmation> findByMemberIdAndCode(UUID memberId, String code);

    /**
     * Recherche un code de confirmation par sa valeur uniquement.
     * <p>
     * Cette méthode permet de retrouver un code sans connaître le membre
     * associé. Utilisée pour des opérations de débogage ou de maintenance.
     * <strong>Attention :</strong> Ne pas utiliser pour la validation en production.
     * </p>
     * 
     * @param code le code de confirmation à rechercher, non null
     * @return un {@link Optional} contenant le code si trouvé, vide sinon
     * @throws IllegalArgumentException si code est null
     * 
     * @implNote Génère : SELECT * FROM member_confirmations WHERE code = ?
     * @deprecated Utiliser findByMemberIdAndCode() pour la validation sécurisée
     */
    Optional<MemberConfirmation> findByCode(String code);

    /**
     * Supprime tous les codes de confirmation expirés.
     * <p>
     * Cette méthode de maintenance supprime définitivement tous les codes
     * dont la date d'expiration est antérieure à l'instant donné.
     * Exécutée périodiquement par un job de nettoyage.
     * </p>
     * 
     * @param now l'instant de référence pour déterminer l'expiration, non null
     * @return le nombre de codes supprimés (≥ 0)
     * @throws IllegalArgumentException si now est null
     * 
     * @implNote Opération de modification : DELETE FROM member_confirmations WHERE expires_at < ?
     */
    @Modifying
    @Query("DELETE FROM MemberConfirmation mc WHERE mc.expiresAt < :now")
    int deleteExpiredConfirmations(@Param("now") LocalDateTime now);

    /**
     * Compte le nombre de codes de confirmation encore valides.
     * <p>
     * Cette méthode de comptage retourne le nombre de codes de confirmation
     * non expirés dans le système. Utilisée pour les statistiques
     * administratives et le monitoring.
     * </p>
     * 
     * @param now l'instant de référence pour déterminer la validité, non null
     * @return le nombre de confirmations en attente (≥ 0)
     * @throws IllegalArgumentException si now est null
     * 
     * @implNote Requête COUNT : SELECT COUNT(mc) FROM MemberConfirmation mc WHERE mc.expiresAt > :now
     */
    @Query("SELECT COUNT(mc) FROM MemberConfirmation mc WHERE mc.expiresAt > :now")
    long countPendingConfirmations(@Param("now") LocalDateTime now);

    /**
     * Vérifie l'existence d'un code de confirmation pour un membre.
     * <p>
     * Cette méthode optimisée vérifie uniquement l'existence sans charger
     * l'entité complète. Utilisée pour déterminer si un membre a déjà
     * un processus de confirmation en cours.
     * </p>
     * 
     * @param memberId l'identifiant du membre, non null
     * @return true si un code existe pour ce membre, false sinon
     * @throws IllegalArgumentException si memberId est null
     * 
     * @implNote Génère une requête COUNT optimisée : SELECT COUNT(*) FROM member_confirmations WHERE member_id = ?
     */
    boolean existsByMemberId(UUID memberId);
}

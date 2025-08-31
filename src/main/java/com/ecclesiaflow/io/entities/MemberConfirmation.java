package com.ecclesiaflow.io.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant un code de confirmation de membre EcclesiaFlow.
 * <p>
 * Cette classe modélise les codes de confirmation temporaires envoyés aux membres
 * pour valider leur adresse email lors de l'inscription. Gère l'expiration
 * automatique et la validation des codes à 6 chiffres.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Entité JPA - Modèle de données des confirmations</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Stockage sécurisé des codes de confirmation temporaires</li>
 *   <li>Gestion de l'expiration automatique des codes</li>
 *   <li>Liaison avec les membres via memberId</li>
 *   <li>Validation de la validité temporelle des codes</li>
 * </ul>
 * 
 * <p><strong>Contraintes de sécurité :</strong></p>
 * <ul>
 *   <li>Codes à usage unique (supprimés après utilisation)</li>
 *   <li>Expiration courte (5-24h selon le contexte)</li>
 *   <li>Format fixe à 6 chiffres pour simplicité utilisateur</li>
 * </ul>
 * 
 * <p><strong>Cycle de vie :</strong></p>
 * <ol>
 *   <li>Création lors de l'inscription ou du renvoi</li>
 *   <li>Envoi par email au membre</li>
 *   <li>Validation par le membre</li>
 *   <li>Suppression automatique après utilisation ou expiration</li>
 * </ol>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, gestion transactionnelle JPA,
 * validation temporelle précise, nettoyage automatique.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Member
 */
@Entity
@Table(name = "member_confirmations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberConfirmation {

    /**
     * Identifiant unique de la confirmation, généré automatiquement via UUID.
     * <p>
     * Assure l'unicité de chaque code de confirmation dans la base de données.
     * </p>
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    /**
     * Identifiant du membre associé à ce code de confirmation.
     * <p>
     * Référence vers l'entité {@link Member} pour laquelle ce code
     * de confirmation a été généré. Permet la liaison entre le code
     * et le membre sans relation JPA directe pour optimiser les performances.
     * </p>
     */
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID memberId;

    /**
     * Code de confirmation à 6 chiffres.
     * <p>
     * Code numérique généré aléatoirement que le membre doit saisir
     * pour confirmer son adresse email. Format fixe de 6 chiffres
     * avec zéros de tête si nécessaire (ex: "012345").
     * </p>
     */
    @Column(nullable = false, length = 6)
    private String code;

    /**
     * Date et heure d'expiration du code de confirmation.
     * <p>
     * Moment précis après lequel le code devient invalide.
     * Généralement fixé à 24h pour l'inscription initiale
     * et 5 minutes pour les renvois de code.
     * </p>
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Date et heure de création du code de confirmation.
     * <p>
     * Généré automatiquement par Hibernate lors de la persistance.
     * Permet de tracer la génération des codes pour audit et débogage.
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Vérifie si le code de confirmation a expiré.
     * <p>
     * Compare l'heure actuelle avec la date d'expiration pour déterminer
     * si le code est encore valide temporellement.
     * </p>
     * 
     * @return true si le code a expiré, false sinon
     * 
     * @implNote Utilise LocalDateTime.now() pour la comparaison en temps réel.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Vérifie si le code de confirmation est encore valide.
     * <p>
     * Méthode de convenance qui inverse le résultat de {@link #isExpired()}
     * pour une lecture plus naturelle du code.
     * </p>
     * 
     * @return true si le code est encore valide, false s'il a expiré
     * 
     * @see #isExpired()
     */
    public boolean isValid() {
        return !isExpired();
    }
}

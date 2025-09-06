package com.ecclesiaflow.io.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une confirmation de membre en base de données.
 * <p>
 * Cette entité gère la persistance des codes de confirmation des membres
 * dans la base de données. Elle contient toutes les annotations JPA nécessaires
 * pour le mapping objet-relationnel et les contraintes de validation.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Entité JPA - Modèle de données des confirmations</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Persistance des codes de confirmation en base de données</li>
 *   <li>Gestion des contraintes d'intégrité référentielle</li>
 *   <li>Horodatage automatique des créations</li>
 *   <li>Validation des données au niveau persistance</li>
 * </ul>
 * 
 * <p><strong>Contraintes de base de données :</strong></p>
 * <ul>
 *   <li>Code obligatoire et non vide</li>
 *   <li>MemberId obligatoire avec contrainte de clé étrangère</li>
 *   <li>Identifiant UUID pour la scalabilité</li>
 *   <li>Dates d'expiration obligatoires</li>
 * </ul>
 * 
 * <p><strong>Cycle de vie JPA :</strong></p>
 * <ol>
 *   <li>Création avec @CreationTimestamp automatique</li>
 *   <li>Persistance avec validation des contraintes</li>
 *   <li>Suppression après utilisation ou expiration</li>
 * </ol>
 * 
 * <p><strong>Garanties :</strong> Thread-safe pour lecture, gestion transactionnelle JPA,
 * contraintes d'intégrité, horodatage automatique.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Entity
@Table(name = "member_confirmations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberConfirmationEntity {

    /**
     * Identifiant unique de la confirmation.
     * Généré automatiquement par Hibernate avec un UUID.
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    /**
     * Identifiant du membre associé à cette confirmation.
     * Référence vers la table des membres.
     */
    @NotNull(message = "L'identifiant du membre est obligatoire")
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID memberId;

    /**
     * Code de confirmation à 6 chiffres.
     * Généré par le service métier et stocké en base.
     */
    @NotBlank(message = "Le code de confirmation est obligatoire")
    @Column(name = "code", length = 6, nullable = false)
    private String code;

    /**
     * Date et heure de création de la confirmation.
     * Générée automatiquement par Hibernate lors de la persistance.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date et heure d'expiration du code de confirmation.
     * Calculée par le service métier (généralement +24h).
     */
    @NotNull(message = "La date d'expiration est obligatoire")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}

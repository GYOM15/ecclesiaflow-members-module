package com.ecclesiaflow.io.persistence.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.ecclesiaflow.business.domain.member.Role;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant un membre EcclesiaFlow en base de données.
 * <p>
 * Cette entité gère la persistance des informations des membres dans la base de données.
 * Elle contient toutes les annotations JPA nécessaires pour le mapping objet-relationnel,
 * les contraintes de validation et la gestion automatique des horodatages.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Entité JPA - Modèle de données des membres</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Persistance des données des membres en base de données</li>
 *   <li>Gestion des contraintes d'intégrité et d'unicité</li>
 *   <li>Horodatage automatique des créations et modifications</li>
 *   <li>Validation des données au niveau persistance</li>
 * </ul>
 * 
 * <p><strong>Contraintes de base de données :</strong></p>
 * <ul>
 *   <li>Email unique et obligatoire</li>
 *   <li>MemberId unique et immutable</li>
 *   <li>Prénom, nom et adresse obligatoires</li>
 *   <li>Rôle obligatoire avec énumération</li>
 *   <li>Identifiants UUID pour la scalabilité</li>
 * </ul>
 * 
 * <p><strong>États de confirmation :</strong></p>
 * <ul>
 *   <li>confirmed : false par défaut, true après validation email</li>
 *   <li>confirmedAt : null jusqu'à confirmation, puis horodatage</li>
 *   <li>passwordSet : false par défaut, true après définition mot de passe</li>
 * </ul>
 * 
 * <p><strong>Cycle de vie JPA :</strong></p>
 * <ol>
 *   <li>Création avec @CreationTimestamp automatique</li>
 *   <li>Mise à jour avec @UpdateTimestamp automatique</li>
 *   <li>Validation des contraintes à chaque persistance</li>
 * </ol>
 * 
 * <p><strong>Garanties :</strong> Thread-safe pour lecture, gestion transactionnelle JPA,
 * contraintes d'intégrité, horodatage automatique, validation des données.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Role
 */
@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "L’email est obligatoire")
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Role role;

    @NotNull(message = "L’identifiant du membre est obligatoire")
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
    private UUID memberId;

    @NotBlank(message = "L’adresse est obligatoire")
    @Column(length = 200, nullable = false)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private boolean confirmed = false;

    @Column
    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean passwordSet = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

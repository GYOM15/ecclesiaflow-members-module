package com.ecclesiaflow.io.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Entité JPA représentant un membre EcclesiaFlow dans la base de données.
 * <p>
 * Cette classe modélise les données de profil des membres de l'application EcclesiaFlow.
 * Elle gère les informations personnelles, le statut de confirmation, et l'intégration
 * avec le module d'authentification via un UUID de référence.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Entité JPA - Modèle de données des membres</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Stockage des informations de profil des membres</li>
 *   <li>Gestion du statut de confirmation des comptes</li>
 *   <li>Référence vers le module d'authentification (memberId)</li>
 *   <li>Horodatage automatique des créations et modifications</li>
 * </ul>
 * 
 * <p><strong>Contraintes de base de données :</strong></p>
 * <ul>
 *   <li>Email unique et obligatoire</li>
 *   <li>MemberId unique pour l'intégration inter-modules</li>
 *   <li>Identifiant UUID pour la scalabilité</li>
 * </ul>
 * 
 * <p><strong>Intégration inter-modules :</strong></p>
 * <ul>
 *   <li>memberId : Référence vers le module d'authentification</li>
 *   <li>email : Identifiant commun entre les modules</li>
 * </ul>
 * 
 * <p><strong>Cycle de vie :</strong></p>
 * <ol>
 *   <li>Création avec confirmed=false</li>
 *   <li>Confirmation via code email</li>
 *   <li>Mise à jour du profil par l'utilisateur</li>
 * </ol>
 * 
 * <p><strong>Garanties :</strong> Thread-safe pour lecture, gestion transactionnelle JPA,
 * horodatage automatique, contraintes d'intégrité.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Role
 * @see MemberConfirmation
 */
@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    /**
     * Identifiant unique du membre, généré automatiquement via UUID.
     * <p>
     * Assure l'unicité du membre dans la base de données.
     * </p>
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    /**
     * Prénom du membre.
     */
    private String firstName;

    /**
     * Nom du membre.
     */
    private String lastName;

    /**
     * Adresse e-mail du membre, utilisée comme identifiant pour la connexion.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Rôle du membre dans l'application, déterminant ses autorisations.
     */
    private Role role;

    /**
     * Identifiant de référence vers le module d'authentification.
     * <p>
     * UUID utilisé pour lier ce membre aux données d'authentification
     * dans le module séparé. Permet la communication inter-modules
     * tout en maintenant la séparation des responsabilités.
     * </p>
     */
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID memberId;

    /**
     * Adresse physique du membre.
     * <p>
     * Champ optionnel pour stocker l'adresse complète du membre.
     * Limité à 200 caractères pour optimiser le stockage.
     * </p>
     */
    @Column(length = 200)
    private String address;

    /**
     * Statut de confirmation du compte membre.
     * <p>
     * Indique si le membre a confirmé son adresse email via le code
     * de confirmation. Par défaut à false lors de la création.
     * </p>
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean confirmed = false;

    /**
     * Date et heure de confirmation du compte.
     * <p>
     * Horodatage précis du moment où le membre a confirmé son compte.
     * Null tant que le compte n'est pas confirmé.
     * </p>
     */
    @Column
    private LocalDateTime confirmedAt;

    /**
     * Date et heure de création du membre.
     * <p>
     * Généré automatiquement par Hibernate lors de la persistance initiale.
     * Immuable après création pour garantir la traçabilité.
     * </p>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date et heure de dernière modification.
     * <p>
     * Mis à jour automatiquement par Hibernate à chaque modification
     * de l'entité. Permet le suivi des changements de profil.
     * </p>
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Retourne l'identifiant du membre, qui est son adresse e-mail.
     *
     * @return Identifiant du membre
     */
    public String getUsername() {
        return email;
    }

    // Getters and setters
    /**
     * Retourne l'identifiant unique du membre.
     *
     * @return Identifiant unique du membre
     */
    public UUID getId() {
        return id;
    }

    /**
     * Définit l'identifiant unique du membre.
     *
     * @param id Identifiant unique du membre
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Retourne le prénom du membre.
     *
     * @return Prénom du membre
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Définit le prénom du membre.
     *
     * @param firstName Prénom du membre
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Retourne le nom du membre.
     *
     * @return Nom du membre
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Définit le nom du membre.
     *
     * @param lastName Nom du membre
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Retourne l'adresse e-mail du membre.
     *
     * @return Adresse e-mail du membre
     */
    public String getEmail() {
        return email;
    }

    /**
     * Définit l'adresse e-mail du membre.
     *
     * @param email Adresse e-mail du membre
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retourne le rôle du membre.
     *
     * @return Rôle du membre
     */
    public Role getRole() {
        return role;
    }

    /**
     * Définit le rôle du membre.
     *
     * @param role Rôle du membre
     */
    public void setRole(Role role) {
        this.role = role;
    }
}

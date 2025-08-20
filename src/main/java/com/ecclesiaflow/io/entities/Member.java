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
 * Cette classe implémente {@link } pour l'intégration avec Spring Security,
 * permettant l'authentification et l'autorisation des membres. Utilise UUID comme
 * identifiant primaire pour garantir l'unicité dans un environnement multi-tenant.
 * </p>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>JPA/Hibernate pour la persistance</li>
 *   <li>Spring Security pour l'authentification</li>
 *   <li>UUID Generator pour les identifiants uniques</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe pour les opérations de lecture, 
 * gestion transactionnelle via JPA.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
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
    private String email;

    /**
     * Rôle du membre dans l'application, déterminant ses autorisations.
     */
    private Role role;

    // Référence vers le membre dans le module d'auth
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID memberId;

    @Column(length = 200)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private boolean confirmed = false;

    @Column
    private LocalDateTime confirmedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

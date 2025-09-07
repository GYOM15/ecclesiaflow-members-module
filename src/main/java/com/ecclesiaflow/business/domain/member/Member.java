package com.ecclesiaflow.business.domain.member;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Entité métier représentant un membre de l'église dans le système EcclesiaFlow.
 * <p>
 * Cette classe constitue l'agrégat racine du domaine membre et encapsule toutes
 * les informations et comportements liés à un membre : profil, statut de confirmation,
 * gestion des mots de passe et mise à jour des données personnelles.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Entité métier - Agrégat racine du domaine membre</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Encapsulation des données de profil membre (nom, email, adresse)</li>
 *   <li>Gestion du statut de confirmation du compte</li>
 *   <li>Gestion du statut de définition du mot de passe</li>
 *   <li>Validation des transitions d'état (confirmation, mot de passe)</li>
 *   <li>Mise à jour immutable des informations via le pattern Builder</li>
 * </ul>
 * 
 * <p><strong>États du cycle de vie :</strong></p>
 * <ol>
 *   <li><strong>Créé</strong> - Membre inscrit mais non confirmé (confirmed = false)</li>
 *   <li><strong>Confirmé</strong> - Email confirmé via code (confirmed = true, confirmedAt défini)</li>
 *   <li><strong>Actif</strong> - Mot de passe défini (passwordSet = true)</li>
 * </ol>
 * 
 * <p><strong>Invariants métier :</strong></p>
 * <ul>
 *   <li>Un membre ne peut être confirmé qu'une seule fois</li>
 *   <li>Le mot de passe ne peut être marqué comme défini qu'une seule fois</li>
 *   <li>L'email doit être unique dans le système (contraint par le repository)</li>
 *   <li>Le rôle par défaut est MEMBER</li>
 * </ul>
 * 
 * <p><strong>Intégration avec d'autres modules :</strong></p>
 * <ul>
 *   <li>Module d'authentification via memberId (UUID)</li>
 *   <li>Module de confirmation via MemberConfirmation</li>
 *   <li>Module d'email via les notifications</li>
 * </ul>
 * 
 * <p><strong>Pattern utilisés :</strong></p>
 * <ul>
 *   <li><strong>Builder Pattern</strong> - Construction flexible et immutable</li>
 *   <li><strong>Value Object</strong> - Objets immutables pour les mises à jour</li>
 *   <li><strong>Domain Entity</strong> - Identité basée sur l'UUID</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see Role
 * @see MembershipRegistration
 * @see MembershipUpdate
 */
@Getter
@Builder(toBuilder = true)
public class Member {

    private final UUID memberId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private final Role role = Role.MEMBER;

    @Builder.Default
    private boolean confirmed = false;

    @Builder.Default
    private boolean passwordSet = false;

    @Getter
    private final UUID id;

    /**
     * Crée une nouvelle instance de Member avec les champs mis à jour.
     * <p>
     * Cette méthode applique le pattern de mise à jour immutable en créant
     * une nouvelle instance avec les valeurs modifiées. Seuls les champs
     * non-null dans l'objet update sont appliqués (patch partiel).
     * </p>
     * 
     * @param update l'objet contenant les nouvelles valeurs, non null
     * @return une nouvelle instance de Member avec les champs mis à jour
     * @throws NullPointerException si update est null
     */
    public Member withUpdatedFields(MembershipUpdate update) {
        return this.toBuilder()
                .firstName(chooseUpdatedValue(update.getFirstName(), this.firstName))
                .lastName(chooseUpdatedValue(update.getLastName(), this.lastName))
                .email(chooseUpdatedValue(update.getEmail(), this.email))
                .address(chooseUpdatedValue(update.getAddress(), this.address))
                .createdAt(this.createdAt)
                .confirmedAt(this.confirmedAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Choisit la nouvelle valeur si elle n'est pas null, sinon garde la valeur actuelle.
     * <p>
     * Méthode utilitaire pour implémenter la logique de patch partiel
     * lors des mises à jour de profil.
     * </p>
     * 
     * @param <T> le type de la valeur
     * @param newValue la nouvelle valeur (peut être null)
     * @param currentValue la valeur actuelle (non null)
     * @return newValue si non null, sinon currentValue
     */
    private <T> T chooseUpdatedValue(T newValue, T currentValue) {
        return Optional.ofNullable(newValue).orElse(currentValue);
    }

    /**
     * Confirme le compte du membre après validation du code de confirmation.
     * <p>
     * Cette méthode effectue la transition d'état vers "confirmé" et
     * enregistre l'horodatage de confirmation. Ne peut être appelée qu'une seule fois.
     * </p>
     * 
     * @throws IllegalStateException si le membre est déjà confirmé
     */
    public void confirm() {
        if (this.confirmed) throw new IllegalStateException("Le membre est déjà confirmé.");
        this.confirmed = true;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * Marque le mot de passe comme défini après sa création par l'utilisateur.
     * <p>
     * Cette méthode effectue la transition d'état vers "mot de passe défini"
     * après que l'utilisateur ait utilisé son token temporaire pour créer
     * son mot de passe. Ne peut être appelée qu'une seule fois.
     * </p>
     * 
     * @throws IllegalStateException si le mot de passe est déjà défini
     */
    public void markPasswordAsSet() {
        if (this.passwordSet) throw new IllegalStateException("Mot de passe déjà défini.");
        this.passwordSet = true;
    }
}
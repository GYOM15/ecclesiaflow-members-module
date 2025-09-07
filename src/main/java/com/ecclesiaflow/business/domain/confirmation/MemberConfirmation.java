package com.ecclesiaflow.business.domain.confirmation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Objet métier représentant une confirmation de membre dans le domaine EcclesiaFlow.
 * <p>
 * Cette classe encapsule toute la logique métier liée aux confirmations de comptes membres.
 * Elle contient les règles de validation, d'expiration et de gestion du cycle de vie
 * des codes de confirmation sans aucune dépendance vers la couche de persistance.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Objet du domaine métier - Logique de confirmation</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Validation de l'expiration des codes de confirmation</li>
 *   <li>Vérification de la validité des codes</li>
 *   <li>Encapsulation des règles métier de confirmation</li>
 *   <li>Gestion immutable des données de confirmation</li>
 * </ul>
 * 
 * <p><strong>Règles métier :</strong></p>
 * <ul>
 *   <li>Un code de confirmation expire après 24 heures</li>
 *   <li>Un code ne peut être utilisé qu'une seule fois</li>
 *   <li>La validation doit être case-sensitive</li>
 * </ul>
 * 
 * <p><strong>Cycle de vie :</strong></p>
 * <ol>
 *   <li>Création avec code généré et expiration définie</li>
 *   <li>Validation lors de la tentative de confirmation</li>
 *   <li>Suppression après utilisation ou expiration</li>
 * </ol>
 * 
 * <p><strong>Garanties :</strong> Immutable, thread-safe, sans dépendances externes.</p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Getter
@Builder(toBuilder = true)
public class MemberConfirmation {

    /**
     * Identifiant unique de la confirmation.
     */
    private final UUID id;

    /**
     * Identifiant du membre associé à cette confirmation.
     */
    private final UUID memberId;

    /**
     * Code de confirmation à 6 chiffres.
     */
    private final String code;

    /**
     * Date et heure de création de la confirmation.
     */
    private final LocalDateTime createdAt;

    /**
     * Date et heure d'expiration du code de confirmation.
     */
    private final LocalDateTime expiresAt;

    /**
     * Vérifie si le code de confirmation a expiré.
     * <p>
     * Compare la date d'expiration avec l'heure actuelle pour déterminer
     * si le code est encore valide temporellement.
     * </p>
     * 
     * @return true si le code a expiré, false sinon
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Vérifie si le code fourni correspond au code de confirmation.
     * <p>
     * Effectue une comparaison case-sensitive du code fourni avec
     * le code stocké dans cette confirmation.
     * </p>
     * 
     * @param providedCode le code à vérifier, non null
     * @return true si le code correspond, false sinon
     * @throws IllegalArgumentException si providedCode est null
     */
    public boolean isValidCode(String providedCode) {
        if (providedCode == null) {
            throw new IllegalArgumentException("Le code fourni ne peut pas être null");
        }
        return code.equals(providedCode);
    }

    /**
     * Vérifie si cette confirmation est valide pour utilisation.
     * <p>
     * Une confirmation est valide si elle n'a pas expiré et si le code
     * fourni correspond au code stocké.
     * </p>
     * 
     * @param providedCode le code à vérifier, non null
     * @return true si la confirmation est valide, false sinon
     */
    public boolean isValid(String providedCode) {
        return !isExpired() && isValidCode(providedCode);
    }

    /**
     * Calcule le temps restant avant expiration en minutes.
     * <p>
     * Utile pour afficher à l'utilisateur combien de temps il lui reste
     * pour utiliser son code de confirmation.
     * </p>
     * 
     * @return le nombre de minutes restantes, 0 si expiré
     */
    public long getMinutesUntilExpiration() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
    }
}

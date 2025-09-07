package com.ecclesiaflow.web.dto;


import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO représentant une requête de confirmation de compte membre via l'API web.
 * <p>
 * Cette classe encapsule le code de confirmation à 6 chiffres saisi par le membre
 * pour valider son adresse email. Utilise les annotations de validation Jakarta
 * pour assurer l'intégrité du format du code avant traitement.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> DTO web - Requête de confirmation</p>
 * 
 * <p><strong>Validations appliquées :</strong></p>
 * <ul>
 *   <li>Code obligatoire et non vide</li>
 *   <li>Format exact : 6 chiffres numériques (regex: \\d{6})</li>
 *   <li>Pas d'espaces ou caractères spéciaux autorisés</li>
 * </ul>
 * 
 * <p><strong>Flux de traitement :</strong></p>
 * <ol>
 *   <li>Réception du code via API REST</li>
 *   <li>Validation automatique par Spring Boot</li>
 *   <li>Conversion en objet métier {@link MembershipConfirmation}</li>
 *   <li>Traitement par {@link com.ecclesiaflow.business.services.MemberConfirmationService}</li>
 * </ol>
 * 
 * <p><strong>Exemples de codes valides :</strong> "123456", "000001", "999999"</p>
 * <p><strong>Exemples de codes invalides :</strong> "12345", "1234567", "abc123", "12 34 56"</p>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, validation automatique, format strict.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MembershipConfirmation
 * @see com.ecclesiaflow.web.controller.MembersConfirmationController
 */
@Data
public class ConfirmationRequest {
    /**
     * Code de confirmation à 6 chiffres saisi par le membre.
     * <p>
     * Code numérique que le membre a reçu par email et qu'il doit saisir
     * pour confirmer son adresse email. Doit être exactement 6 chiffres
     * numériques sans espaces ni caractères spéciaux.
     * </p>
     * 
     * @implNote Validation par regex \\d{6} pour garantir le format exact.
     */
    @NotBlank(message = "Le code de confirmation est obligatoire")
    @Pattern(regexp = "\\d{6}", message = "Le code doit contenir exactement 6 chiffres")
    private String code;
}

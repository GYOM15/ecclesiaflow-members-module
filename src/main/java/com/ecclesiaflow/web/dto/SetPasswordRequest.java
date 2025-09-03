package com.ecclesiaflow.web.dto;


import com.ecclesiaflow.business.services.MemberPasswordService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO représentant une requête de définition de mot de passe initial pour un membre EcclesiaFlow.
 * <p>
 * Cette classe encapsule les données nécessaires pour permettre à un membre confirmé
 * de définir son mot de passe initial via un token temporaire. Utilisée dans le
 * processus post-confirmation d'inscription.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> DTO web - Requête de définition de mot de passe</p>
 * 
 * <p><strong>Flux d'utilisation :</strong></p>
 * <ol>
 *   <li>Membre confirme son inscription → reçoit token temporaire</li>
 *   <li>Membre utilise token pour accéder au formulaire de mot de passe</li>
 *   <li>Soumission de cette requête avec email, mot de passe et token</li>
 *   <li>Validation et délégation au module d'authentification</li>
 * </ol>
 * 
 * <p><strong>Validations appliquées :</strong></p>
 * <ul>
 *   <li>Email : format valide et obligatoire</li>
 *   <li>Mot de passe : 8+ caractères, complexité (maj/min/chiffre/spécial)</li>
 *   <li>Token temporaire : présence obligatoire</li>
 * </ul>
 * 
 * <p><strong>Sécurité :</strong></p>
 * <ul>
 *   <li>Token temporaire à durée limitée (1h)</li>
 *   <li>Validation côté serveur des critères de mot de passe</li>
 *   <li>Transmission sécurisée vers module d'authentification</li>
 * </ul>
 * 
 * <p><strong>Intégration :</strong> Utilisée par {@link com.ecclesiaflow.web.controller.MemberPasswordController}
 * et traitée par {@link MemberPasswordService}.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.web.controller.MemberPasswordController
 * @see MemberPasswordService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour définir le mot de passe initial")
public class SetPasswordRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Schema(description = "Email du membre", example = "membre@ecclesiaflow.com")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
            message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un chiffre et un caractère spécial"
    )
    @Schema(description = "Nouveau mot de passe", example = "MonMotDePasse123")
    private String password;

    @NotBlank(message = "Le token temporaire est obligatoire")
    @Schema(description = "Token temporaire pour autoriser l'opération", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String temporaryToken;
}

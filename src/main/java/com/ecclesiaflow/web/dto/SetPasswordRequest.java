package com.ecclesiaflow.web.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour définir le mot de passe initial d'un membre.
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
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

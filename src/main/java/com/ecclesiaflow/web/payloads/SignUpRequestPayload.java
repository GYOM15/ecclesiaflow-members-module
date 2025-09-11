package com.ecclesiaflow.web.payloads;

import com.ecclesiaflow.business.domain.member.MembershipRegistration;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO représentant une requête d'inscription d'un nouveau membre via l'API web.
 * <p>
 * Cette classe encapsule les données d'inscription envoyées par le client lors
 * d'une demande de création de compte. Utilise les annotations de validation Jakarta
 * pour assurer l'intégrité des données avant traitement.
 * </p>
 * 
 * <p><strong>Validations appliquées :</strong></p>
 * <ul>
 *   <li>Prénom et nom : obligatoires, entre 2 et 50 caractères</li>
 *   <li>Email : obligatoire, format valide, maximum 100 caractères</li>
 *   <li>Mot de passe : obligatoire, entre 6 et 100 caractères</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Réception des données d'inscription via formulaire web</li>
 *   <li>Validation automatique par Spring Boot</li>
 *   <li>Conversion en objet métier {@link MembershipRegistration}</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, validation automatique.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Data
public class SignUpRequestPayload {
    /**
     * Prénom du membre.
     * <p>
     * Obligatoire, compris entre 2 et 50 caractères.
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    /**
     * Nom de famille du membre.
     * <p>
     * Obligatoire, compris entre 2 et 50 caractères.
     */
    @NotBlank(message = "Le nom de famille est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom de famille doit contenir entre 2 et 50 caractères")
    private String lastName;

    /**
     * Adresse email du membre.
     * <p>
     * Obligatoire, format valide, maximum 100 caractères.
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    /**
     * Adresse du membre.
     * <p>
     * Optionnelle, maximum 200 caractères.
     */
    @Size(min = 10, max = 200, message = "L'adresse doit contenir entre 10 et 200 caractères")
    private String address;

    /**
     * Adresse du membre.
     * <p>
     * Optionnelle
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Format de téléphone invalide")
    private String phoneNumber;
}

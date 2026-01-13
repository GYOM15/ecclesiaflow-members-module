package com.ecclesiaflow.business.domain.communication;

import java.util.UUID;

/**
 * Port (interface) pour l'envoi d'emails - Architecture Hexagonale.
 * 
 * <p>Cette interface définit le contrat pour l'envoi d'emails depuis le module Members.
 * L'implémentation concrète est injectée par la couche Application (inversion de dépendance).</p>
 * 
 * <p><strong>Rôle architectural :</strong> Port de sortie (Output Port) du domaine</p>
 * 
 * <p><strong>Implémentation :</strong></p>
 * <ul>
 *   <li>{@code EmailGrpcClient} - Communication gRPC avec le module Email</li>
 * </ul>
 * 
 * <p><strong>Usage typique :</strong></p>
 * <pre>{@code
 * @Autowired
 * private EmailClient emailClient;
 * 
 * UUID emailId = emailClient.sendConfirmationEmail(
 *     "user@example.com",
 *     "https://app.com/confirm?token=abc123"
 * );
 * }</pre>
 * 
 * <p><strong>Note :</strong> Les implémentations peuvent être synchrones ou asynchrones.
 * Le retour UUID permet le tracking de l'email envoyé.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface EmailClient {

    /**
     * Envoie un email de confirmation d'adresse email.
     * 
     * @param email adresse email du destinataire
     * @param confirmationUrl lien de confirmation complet avec token
     * @return UUID de l'email envoyé (pour tracking)
     * @throws com.ecclesiaflow.business.exceptions.EmailServiceException si l'envoi échoue
     */
    UUID sendConfirmationEmail(String email, String confirmationUrl);

}

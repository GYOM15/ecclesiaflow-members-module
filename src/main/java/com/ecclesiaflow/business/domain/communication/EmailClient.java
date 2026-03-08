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
     * Sends an email confirmation to verify the user's email address.
     * 
     * @param email recipient email address
     * @param confirmationUrl complete confirmation link with token
     * @param firstName recipient's first name for personalization
     * @return UUID of the sent email (for tracking)
     * @throws com.ecclesiaflow.business.exceptions.EmailServiceException if sending fails
     */
    UUID sendConfirmationEmail(String email, String confirmationUrl, String firstName);

    /**
     * Sends a welcome email after account activation.
     * 
     * @param email recipient email address
     * @param firstName recipient's first name for personalization
     * @return UUID of the sent email (for tracking)
     * @throws com.ecclesiaflow.business.exceptions.EmailServiceException if sending fails
     */
    UUID sendWelcomeEmail(String email, String firstName);

}

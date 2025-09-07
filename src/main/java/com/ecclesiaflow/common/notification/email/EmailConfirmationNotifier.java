package com.ecclesiaflow.common.notification.email;

import com.ecclesiaflow.common.notification.ConfirmationNotifier;
import com.ecclesiaflow.business.domain.communication.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implémentation de {@link ConfirmationNotifier} pour l'envoi de codes via email.
 * <p>
 * Cette classe respecte le principe SRP en se concentrant uniquement sur
 * l'envoi de codes de confirmation par email. Délègue la logique d'envoi
 * d'email au service spécialisé {@link EmailService} tout en implémentant
 * le contrat d'envoi de notifications de confirmation.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Implémentation de service - Envoi email de codes</p>
 * 
 * <p><strong>Responsabilité unique :</strong></p>
 * <ul>
 *   <li>Envoi de codes de confirmation via email uniquement</li>
 *   <li>Adaptation du contrat {@link ConfirmationNotifier} pour l'email</li>
 *   <li>Délégation au service email spécialisé</li>
 * </ul>
 * 
 * <p><strong>Avantages SRP :</strong></p>
 * <ul>
 *   <li>Responsabilité unique - Envoi email seulement</li>
 *   <li>Testabilité isolée du canal email</li>
 *   <li>Réutilisabilité dans d'autres contextes d'envoi</li>
 *   <li>Évolution indépendante de la logique email</li>
 * </ul>
 * 
 * <p><strong>Pattern Strategy :</strong> Implémentation concrète du pattern Strategy
 * défini par {@link ConfirmationNotifier}, permettant l'envoi via email.</p>
 * 
 * <p><strong>Dépendances :</strong></p>
 * <ul>
 *   <li>{@link EmailService} - Service spécialisé pour l'envoi d'emails</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, délégation propre, gestion d'erreurs
 * déléguée au service email, respect du contrat d'interface.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see ConfirmationNotifier
 * @see EmailService
 */
@Service
@RequiredArgsConstructor
public class EmailConfirmationNotifier implements ConfirmationNotifier {

    private final EmailService emailService;

    /**
     * {@inheritDoc}
     * <p>
     * Implémentation spécifique pour l'envoi via email. Délègue l'envoi
     * effectif au service {@link EmailService} qui gère tous les détails
     * techniques de l'envoi d'email (SMTP, templates, etc.).
     * </p>
     * 
     * @param email l'adresse email du destinataire, non null
     * @param confirmationCode le code de confirmation à envoyer, non null
     * @param firstName le prénom du membre pour personnalisation, non null
     * 
     * @implNote Toute la logique d'envoi d'email (formatage, templates, SMTP)
     *           est déléguée au {@link EmailService} pour respecter SRP.
     */
    @Override
    public void sendCode(String email, String confirmationCode, String firstName) {
        emailService.sendConfirmationCode(email, confirmationCode, firstName);
    }
}

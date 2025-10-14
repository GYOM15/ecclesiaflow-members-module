package com.ecclesiaflow.io.notification.email;

import com.ecclesiaflow.business.domain.communication.ConfirmationNotifier;
import com.ecclesiaflow.business.domain.communication.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implémentation de {@link ConfirmationNotifier} pour l'envoi de liens de confirmation via email.
 * <p>
 * Cette classe respecte le principe SRP en se concentrant uniquement sur
 * l'envoi de liens de confirmation sécurisés par email. Délègue la logique d'envoi
 * d'email au service spécialisé {@link EmailService} tout en implémentant
 * le contrat d'envoi de notifications de confirmation.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Implémentation de service - Envoi email de liens</p>
 * 
 * <p><strong>Responsabilité unique :</strong></p>
 * <ul>
 *   <li>Envoi de liens de confirmation via email uniquement</li>
 *   <li>Construction des URLs de confirmation</li>
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
    
    @Value("${ecclesiaflow.members.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * {@inheritDoc}
     * <p>
     * Implémentation pour l'envoi d'un lien de confirmation sécurisé via email asynchrone.
     * Construit l'URL complète avec le token UUID et délègue l'envoi au service
     * {@link EmailService} qui gère tous les détails techniques de l'envoi d'email.
     * <strong>Exécution asynchrone :</strong> L'envoi se fait en arrière-plan,
     * permettant une réponse immédiate à l'utilisateur.
     * </p>
     * 
     * <p><strong>Format du lien :</strong> 
     * {@code {baseUrl}/ecclesiaflow/members/confirmation?token={uuid}}
     * </p>
     * 
     * <p><strong>Exemple :</strong>
     * {@code https://app.ecclesiaflow.com/ecclesiaflow/members/confirmation?token=550e8400-e29b-41d4-a716-446655440000}
     * </p>
     * 
     * @param email l'adresse email du destinataire, non null
     * @param confirmationToken le token de confirmation UUID à inclure dans le lien, non null
     * @param firstName le prénom du membre pour personnalisation, non null
     * 
     * @implNote L'URL de base est configurée via la propriété
     *           {@code ecclesiaflow.members.base-url} (par défaut: http://localhost:8080).
     *           Toute la logique d'envoi d'email (formatage, templates, SMTP)
     *           est déléguée au {@link EmailService} pour respecter SRP.
     *           L'exécution asynchrone améliore l'expérience utilisateur.
     */
    @Override
    public void sendConfirmationLink(String email, UUID confirmationToken, String firstName) {
        String confirmationUrl = buildConfirmationUrl(confirmationToken);
        emailService.sendConfirmationCode(email, confirmationUrl, firstName);
    }

    /**
     * Construit l'URL complète de confirmation avec le token UUID.
     * 
     * @param token le token de confirmation UUID
     * @return l'URL complète de confirmation
     */
    private String buildConfirmationUrl(UUID token) {
        return String.format("%s/ecclesiaflow/members/confirmation?token=%s", baseUrl, token);
    }
}

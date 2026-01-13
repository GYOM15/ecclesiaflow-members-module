package com.ecclesiaflow.application.handlers;

import com.ecclesiaflow.business.domain.communication.EmailClient;
import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Handler d'événements pour l'inscription de nouveaux membres.
 * 
 * <p>Cette classe écoute les événements métier {@link MemberRegisteredEvent}
 * et orchestre les actions techniques nécessaires (construction d'URLs, envoi d'emails via gRPC).</p>
 * 
 * <p><strong>Architecture :</strong></p>
 * <pre>
 * Business (Services) → publie MemberRegisteredEvent
 *         ↓ (après transaction COMMIT)
 * Application (Handler) → écoute + transforme + orchestre
 *         ↓
 * Infrastructure (EmailGrpcClient) → appel gRPC vers Module Email
 * </pre>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Transformation des événements métier en actions techniques</li>
 *   <li>Construction des URLs de confirmation (concerne Application/Web)</li>
 *   <li>Orchestration des appels vers l'infrastructure (EmailClient)</li>
 *   <li>Exécution asynchrone APRÈS commit pour ne pas bloquer les transactions</li>
 * </ul>
 * 
 * <p><strong>Garanties transactionnelles :</strong></p>
 * <ul>
 *   <li>Utilise {@code @TransactionalEventListener(AFTER_COMMIT)}</li>
 *   <li>Email envoyé UNIQUEMENT si la transaction d'inscription réussit</li>
 *   <li>Si rollback → handler jamais appelé, pas d'email parasite envoyé</li>
 * </ul>
 * 
 * <p><strong>Note :</strong> Similaire à {@code PasswordEventHandler} du module Auth
 * pour garantir une architecture cohérente entre modules.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberRegisteredEvent
 * @see EmailClient
 * @see TransactionalEventListener
 */
@Component
@RequiredArgsConstructor
public class MemberRegistrationEventHandler {

    private final EmailClient emailClient;
    
    @Value("${ecclesiaflow.members.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Gère l'événement d'inscription d'un nouveau membre.
     * 
     * <p>Processus :</p>
     * <ol>
     *   <li>Construction du lien de confirmation avec token UUID</li>
     *   <li>Envoi de l'email de confirmation via EmailClient (gRPC)</li>
     * </ol>
     * 
     * <p><strong>Garanties transactionnelles :</strong></p>
     * <ul>
     *   <li>Exécuté UNIQUEMENT après commit réussi de la transaction</li>
     *   <li>Si rollback → méthode jamais appelée, pas d'email envoyé</li>
     * </ul>
     * 
     * <p><strong>Exécution asynchrone :</strong> Utilise le pool de threads
     * {@code emailTaskExecutor} pour ne pas bloquer le thread principal.</p>
     * 
     * <p><strong>Gestion des erreurs :</strong> Les exceptions sont capturées
     * pour ne pas impacter la transaction métier (déjà committée). Le logging
     * est géré par les aspects AOP.</p>
     * 
     * @param event événement contenant email, token et prénom du membre
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("emailTaskExecutor")
    public void handleMemberRegistered(MemberRegisteredEvent event) {
        try {
            String confirmationUrl = buildConfirmationUrl(event.getConfirmationToken().toString());
            emailClient.sendConfirmationEmail(event.getEmail(), confirmationUrl);
        } catch (Exception e) {
            // Exception capturée pour ne pas impacter la transaction métier
            // Le logging est géré par les aspects AOP
        }
    }

    /**
     * Construit l'URL complète de confirmation avec le token.
     * 
     * @param token token de confirmation UUID
     * @return URL complète de confirmation
     */
    private String buildConfirmationUrl(String token) {
        return String.format("%s/ecclesiaflow/members/confirmation?token=%s", baseUrl, token);
    }
}

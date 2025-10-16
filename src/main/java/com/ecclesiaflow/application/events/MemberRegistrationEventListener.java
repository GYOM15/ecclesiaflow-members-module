package com.ecclesiaflow.application.events;

import com.ecclesiaflow.business.domain.communication.ConfirmationNotifier;
import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Gestionnaire d'événements pour les inscriptions de membres avec exécution asynchrone.
 * <p>
 * Ce listener écoute les événements {@link MemberRegisteredEvent} et déclenche
 * les actions post-inscription (envoi d'email de confirmation) de manière asynchrone
 * UNIQUEMENT après le commit réussi de la transaction d'inscription.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Event Listener - Async Post-Transaction Processing</p>
 * 
 * <p><strong>Garanties de cohérence :</strong></p>
 * <ul>
 *   <li>✓ Email envoyé SEULEMENT si transaction committée avec succès</li>
 *   <li>✓ Si rollback → événement ignoré, pas d'email envoyé</li>
 *   <li>✓ Exécution asynchrone après commit pour performance optimale</li>
 *   <li>✓ Thread principal libéré immédiatement après commit (réponse HTTP rapide)</li>
 * </ul>
 * 
 * <p><strong>Flux d'exécution :</strong></p>
 * <pre>
 * 1. Transaction COMMIT ✓
 * 2. @TransactionalEventListener(AFTER_COMMIT) déclenché
 * 3. @Async → Tâche mise en queue du thread pool dédié
 * 4. → Réponse HTTP 201 retournée immédiatement
 * 5. Thread pool "emailTaskExecutor" exécute l'envoi email en background
 * 6. Email envoyé (ou erreur capturée par AsyncEmailLoggingAspect)
 * </pre>
 * 
 * <p><strong>Gestion des erreurs :</strong></p>
 * <ul>
 *   <li>Exceptions capturées et loggées via {@code AsyncEmailLoggingAspect}</li>
 *   <li>Pas d'impact sur la transaction (déjà committée)</li>
 *   <li>Monitoring et alerting via AOP aspect</li>
 * </ul>
 * 
 * <p><strong>⚠️ Note temporaire :</strong> Cette implémentation sera remplacée
 * par un service d'emailing dédié avec outbox pattern pour gestion robuste
 * à grande échelle (retry, idempotence, dead letter queue).</p>
 * 
 * <p><strong>Note logging :</strong> Aucun log direct dans cette classe.
 * Tous les logs sont gérés par {@code AsyncEmailLoggingAspect} pour respecter
 * la séparation des concerns et centraliser le logging dans la couche application.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see MemberRegisteredEvent
 * @see TransactionalEventListener
 * @see Async
 * @see com.ecclesiaflow.application.logging.aspect.AsyncEmailLoggingAspect
 */
@Component
@RequiredArgsConstructor
public class MemberRegistrationEventListener {
    
    private final ConfirmationNotifier confirmationNotifier;
    
    /**
     * Traite l'événement d'inscription de membre en envoyant l'email de confirmation.
     * <p>
     * Cette méthode est déclenchée APRÈS le commit de la transaction d'inscription
     * grâce à {@code @TransactionalEventListener(phase = AFTER_COMMIT)}.
     * L'exécution est asynchrone via {@code @Async("emailTaskExecutor")} pour
     * libérer immédiatement le thread principal.
     * </p>
     * 
     * <p><strong>Comportement selon état transaction :</strong></p>
     * <ul>
     *   <li>Transaction COMMIT → Cette méthode est appelée ✓</li>
     *   <li>Transaction ROLLBACK → Cette méthode N'EST PAS appelée ✓</li>
     * </ul>
     * 
     * <p><strong>Timing :</strong></p>
     * <ul>
     *   <li>Déclenché: Immédiatement après commit DB</li>
     *   <li>Exécuté: Quand un thread du pool "emailTaskExecutor" est disponible</li>
     *   <li>Durée: ~2-5 secondes (SMTP), mais n'impacte pas réponse HTTP</li>
     * </ul>
     * 
     * <p><strong>Gestion des exceptions :</strong></p>
     * <ul>
     *   <li>Exception SMTP → Capturée par AsyncEmailLoggingAspect</li>
     *   <li>Loggée avec contexte complet pour debugging</li>
     *   <li>Pas de propagation (transaction déjà committée)</li>
     * </ul>
     * 
     * @param event l'événement contenant les données d'inscription (email, token, prénom)
     * 
     * @implNote L'ordre d'annotations est important:
     *           1. @TransactionalEventListener → Garantit exécution après commit
     *           2. @Async → Rend l'exécution asynchrone
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("emailTaskExecutor")
    public void handleMemberRegistered(MemberRegisteredEvent event) {
        confirmationNotifier.sendConfirmationLink(
            event.getEmail(),
            event.getConfirmationToken(),
            event.getFirstName()
        );
    }
}

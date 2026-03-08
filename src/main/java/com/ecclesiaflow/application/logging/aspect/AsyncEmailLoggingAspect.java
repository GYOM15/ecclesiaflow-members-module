package com.ecclesiaflow.application.logging.aspect;

import com.ecclesiaflow.business.domain.events.MemberRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Aspect AOP spécialisé dans le logging des événements asynchrones post-transaction.
 * <p>
 * Cette classe implémente un aspect dédié au logging des événements d'inscription
 * de membres traités de manière asynchrone après commit de transaction. Permet
 * de tracer le cycle de vie complet des envois d'emails de confirmation.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Aspect - Audit des événements async</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Logger les événements MemberRegistered avant traitement async</li>
 *   <li>Tracer les succès d'envoi d'emails post-transaction</li>
 *   <li>Capturer et logger les échecs d'envoi d'emails async</li>
 *   <li>Fournir visibilité complète sur le pipeline async</li>
 * </ul>
 * 
 * <p><strong>Événements tracés :</strong></p>
 * <ul>
 *   <li>Réception de l'événement MemberRegistered (après commit)</li>
 *   <li>Début du traitement asynchrone dans le thread pool</li>
 *   <li>Succès de l'envoi d'email</li>
 *   <li>Échecs et exceptions durant l'envoi</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, logging asynchrone, séparation des concerns.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component("asyncEmailLoggingAspect")
public class AsyncEmailLoggingAspect {
    
    /**
     * Pointcut pour le traitement des événements MemberRegistered.
     * <p>
     * Intercepte tous les appels à {@code handleMemberRegistered} du handler
     * pour tracer le cycle de vie complet de l'envoi d'email asynchrone via gRPC.
     * </p>
     */
    @Pointcut("execution(* com.ecclesiaflow.application.handlers.MemberRegistrationEventHandler.handleMemberRegistered(..))")
    public void memberRegisteredEventHandling() {}
    
    /**
     * Log avant le traitement async de l'événement MemberRegistered.
     * <p>
     * Enregistre la réception de l'événement et l'email du destinataire
     * pour traçabilité complète du processus d'inscription.
     * </p>
     * 
     * <p><strong>Timeline :</strong></p>
     * <pre>
     * 1. Transaction COMMIT ✓
     * 2. Événement MemberRegistered publié
     * 3. → CE LOG (événement reçu, mise en queue)
     * 4. Thread pool exécute la tâche
     * 5. Email envoyé
     * </pre>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     */
    @Before("memberRegisteredEventHandling()")
    public void logBeforeAsyncEmailSending(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0 && args[0] instanceof MemberRegisteredEvent event) {
            log.info("ASYNC EVENT: MemberRegistered event received for email: {} - Queuing async email task", 
                     event.email());
        }
    }
    
    /**
     * Log après envoi réussi de l'email asynchrone.
     * <p>
     * Confirme que l'email de confirmation a été envoyé avec succès
     * après le commit de la transaction d'inscription.
     * </p>
     * 
     * <p><strong>Garanties :</strong></p>
     * <ul>
     *   <li>Transaction déjà committée ✓</li>
     *   <li>Membre enregistré en DB ✓</li>
     *   <li>Email envoyé avec succès ✓</li>
     * </ul>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     */
    @AfterReturning("memberRegisteredEventHandling()")
    public void logAfterSuccessfulAsyncEmailSending(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0 && args[0] instanceof MemberRegisteredEvent event) {
            log.info("ASYNC EVENT: Confirmation email sent successfully to: {} (async task completed)", 
                     event.email());
        }
    }
    
    /**
     * Log en cas d'échec d'envoi d'email asynchrone.
     * <p>
     * Enregistre les erreurs d'envoi d'email qui se produisent après
     * le commit de la transaction. Important: la transaction EST déjà
     * committée, donc le membre existe en DB même si l'email échoue.
     * </p>
     * 
     * <p><strong>Actions recommandées en cas d'échec :</strong></p>
     * <ul>
     *   <li>Vérifier la configuration SMTP</li>
     *   <li>Vérifier la connectivité réseau</li>
     *   <li>Considérer un système de retry (dead letter queue)</li>
     *   <li>Alerter l'équipe ops si échecs récurrents</li>
     * </ul>
     * 
     * <p><strong>⚠️ Important :</strong> Le membre EST enregistré en DB.
     * L'échec email ne doit pas empêcher l'inscription. L'utilisateur peut
     * redemander un lien de confirmation via l'endpoint dédié.</p>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     * @param exception l'exception qui a causé l'échec
     */
    @AfterThrowing(pointcut = "memberRegisteredEventHandling()", throwing = "exception")
    public void logFailedAsyncEmailSending(JoinPoint joinPoint, Throwable exception) {
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0 && args[0] instanceof MemberRegisteredEvent event) {
            String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
            log.error("ASYNC EVENT: Failed to send confirmation email to: {} (member IS registered in DB) - {}", 
                      event.email(),
                      errorMessage, 
                      exception);
            log.warn("ASYNC EVENT: Member can request a new confirmation link via resend endpoint");
        }
    }
}

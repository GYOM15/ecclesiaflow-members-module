package com.ecclesiaflow.business.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Aspect AOP spécialisé dans le logging des opérations métier critiques EcclesiaFlow.
 * <p>
 * Cette classe implémente un aspect dédié au logging des opérations métier sensibles
 * comme l'inscription, l'authentification et le rafraîchissement de tokens.
 * Séparé du logging technique général pour respecter le principe de responsabilité unique.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Aspect métier - Audit des opérations critiques</p>
 * 
 * <p><strong>Dépendances critiques :</strong></p>
 * <ul>
 *   <li>Spring AOP - Framework de programmation orientée aspect</li>
 *   <li>SLF4J/Logback - Framework de logging métier</li>
 *   <li>Services d'authentification - Cibles des pointcuts</li>
 * </ul>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Audit des inscriptions de nouveaux membres</li>
 *   <li>Traçabilité des tentatives d'authentification</li>
 *   <li>Logging des opérations de rafraîchissement de tokens</li>
 *   <li>Capture des échecs d'opérations métier</li>
 * </ul>
 * 
 * <p><strong>Cas d'utilisation typiques :</strong></p>
 * <ul>
 *   <li>Audit de sécurité des comptes utilisateurs</li>
 *   <li>Traçabilité des connexions pour conformité</li>
 *   <li>Détection des tentatives d'intrusion</li>
 *   <li>Analyse des patterns d'utilisation</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, logging asynchrone, séparation métier/technique.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component("membersBusinessOperationLoggingAspect")
public class BusinessOperationLoggingAspect {

    /**
     * Pointcut pour l'enregistrement de nouveaux membres.
     * <p>
     * Intercepte tous les appels à la méthode {@code registerMember} du service
     * d'inscription pour auditer les créations de comptes membres.
     * </p>
     */
    @Pointcut("execution(* com.ecclesiaflow.business.services.impl.MemberServiceImpl.registerMember(..))")
    public void memberRegistration() {}

    /**
     * Pointcut pour les appels au module d'authentification.
     * <p>
     * Intercepte toutes les méthodes du service d'authentification externe
     * pour tracer les interactions inter-modules critiques.
     * </p>
     */
    @Pointcut("execution(* com.ecclesiaflow.web.client.AuthClient.*(..))")
    public void authModuleCalls() {}

    // === CONSEILS POUR L'ENREGISTREMENT DES MEMBRES ===
    
    /**
     * Log avant tentative d'enregistrement d'un nouveau membre.
     * <p>
     * Enregistre le début du processus d'inscription pour audit et traçabilité.
     * </p>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     */
    @Before("memberRegistration()")
    public void logBeforeMemberRegistration(JoinPoint joinPoint) {
        log.info("BUSINESS: Tentative d'enregistrement d'un nouveau membre");
    }

    /**
     * Log après enregistrement réussi d'un nouveau membre.
     * <p>
     * Confirme la création réussie du compte membre pour audit de sécurité.
     * </p>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     */
    @AfterReturning("memberRegistration()")
    public void logAfterSuccessfulRegistration(JoinPoint joinPoint) {
        log.info("BUSINESS: Nouveau membre enregistré avec succès");
    }

    /**
     * Log en cas d'échec d'enregistrement d'un membre.
     * <p>
     * Enregistre les erreurs d'inscription pour analyse et debugging.
     * </p>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     * @param exception l'exception qui a causé l'échec
     */
    @AfterThrowing(pointcut = "memberRegistration()", throwing = "exception")
    public void logFailedRegistration(JoinPoint joinPoint, Throwable exception) {
        log.warn("BUSINESS: Échec de l'enregistrement du membre - {}", exception.getMessage());
    }

    // === CONSEILS POUR LES APPELS AU MODULE D'AUTHENTIFICATION ===
    
    /**
     * Log avant appel au module d'authentification.
     * <p>
     * Trace les appels inter-modules pour debugging et monitoring des intégrations.
     * </p>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     */
    @Before("authModuleCalls()")
    public void logBeforeAuthModuleCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.debug("AUTH MODULE: Appel à {} avec arguments: {}", methodName, joinPoint.getArgs());
    }

    /**
     * Log après appel réussi au module d'authentification.
     * <p>
     * Confirme le succès des opérations d'authentification pour audit.
     * </p>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     * @param result le résultat retourné par la méthode (peut être null)
     */
    @AfterReturning(pointcut = "authModuleCalls()", returning = "result")
    public void logAfterSuccessfulAuthModuleCall(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        if (result != null) {
            log.debug("AUTH MODULE: {} a réussi avec résultat: {}", methodName, result);
        } else {
            log.debug("AUTH MODULE: {} exécuté avec succès", methodName);
        }
    }

    /**
     * Log en cas d'erreur lors d'appel au module d'authentification.
     * <p>
     * Enregistre les échecs d'intégration pour diagnostic et alerting.
     * </p>
     * 
     * @param joinPoint point de jonction contenant les détails de l'appel
     * @param exception l'exception qui a causé l'échec
     */
    @AfterThrowing(pointcut = "authModuleCalls()", throwing = "exception")
    public void logAuthModuleError(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        log.warn("AUTH MODULE: Échec de l'appel à {} - {}", methodName, exception.getMessage());
    }
}

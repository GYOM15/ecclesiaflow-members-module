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
     * Pointcut pour l'enregistrement de nouveaux membres
     */
    @Pointcut("execution(* com.ecclesiaflow.business.services.impl.MemberServiceImpl.registerMember(..))")
    public void memberRegistration() {}

    /**
     * Pointcut pour les appels au module d'authentification
     */
    @Pointcut("execution(* com.ecclesiaflow.business.services.impl.AuthModuleService.*(..))")
    public void authModuleCalls() {}

    // === CONSEILS POUR L'ENREGISTREMENT DES MEMBRES ===
    
    @Before("memberRegistration()")
    public void logBeforeMemberRegistration(JoinPoint joinPoint) {
        log.info("BUSINESS: Tentative d'enregistrement d'un nouveau membre");
    }

    @AfterReturning("memberRegistration()")
    public void logAfterSuccessfulRegistration(JoinPoint joinPoint) {
        log.info("BUSINESS: Nouveau membre enregistré avec succès");
    }

    @AfterThrowing(pointcut = "memberRegistration()", throwing = "exception")
    public void logFailedRegistration(JoinPoint joinPoint, Throwable exception) {
        log.warn("BUSINESS: Échec de l'enregistrement du membre - {}", exception.getMessage());
    }

    // === CONSEILS POUR LES APPELS AU MODULE D'AUTHENTIFICATION ===
    
    @Before("authModuleCalls()")
    public void logBeforeAuthModuleCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.debug("AUTH MODULE: Appel à {} avec arguments: {}", methodName, joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "authModuleCalls()", returning = "result")
    public void logAfterSuccessfulAuthModuleCall(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        if (result != null) {
            log.debug("AUTH MODULE: {} a réussi avec résultat: {}", methodName, result);
        } else {
            log.debug("AUTH MODULE: {} exécuté avec succès", methodName);
        }
    }

    @AfterThrowing(pointcut = "authModuleCalls()", throwing = "exception")
    public void logAuthModuleError(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        log.warn("AUTH MODULE: Échec de l'appel à {} - {}", methodName, exception.getMessage());
    }
}

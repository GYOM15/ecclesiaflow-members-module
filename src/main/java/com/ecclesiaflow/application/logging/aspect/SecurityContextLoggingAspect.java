package com.ecclesiaflow.application.logging.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Aspect AOP pour la journalisation centralisée des opérations de contexte de sécurité.
 * <p>
 * Cet aspect intercepte toutes les méthodes de {@link com.ecclesiaflow.business.security.AuthenticatedUserContextProvider}
 * pour fournir une journalisation complète sans polluer le code métier.
 * </p>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Journalisation de l'extraction du memberId depuis le JWT</li>
 *   <li>Journalisation de l'extraction des scopes depuis le JWT</li>
 *   <li>Journalisation des erreurs de parsing JWT</li>
 *   <li>Journalisation des erreurs de contexte HTTP</li>
 * </ul>
 * 
 * <p><strong>Architecture :</strong></p>
 * <ul>
 *   <li><strong>Couche Application :</strong> Aspect de logging (cross-cutting concern)</li>
 *   <li><strong>Couche Business :</strong> AuthenticatedUserContextProvider (logique métier pure)</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.business.security.AuthenticatedUserContextProvider
 */
@Aspect
@Component
@Slf4j
public class SecurityContextLoggingAspect {

    /**
     * Journalise les tentatives d'extraction du memberId.
     */
    @Before("execution(* com.ecclesiaflow.business.security.AuthenticatedUserContextProvider.getAuthenticatedMemberId())")
    public void logBeforeGetMemberId(JoinPoint joinPoint) {
        log.debug("Extracting authenticated member ID from JWT");
    }

    /**
     * Journalise le succès de l'extraction du memberId.
     */
    @AfterReturning(
        pointcut = "execution(* com.ecclesiaflow.business.security.AuthenticatedUserContextProvider.getAuthenticatedMemberId())",
        returning = "memberId"
    )
    public void logAfterGetMemberId(UUID memberId) {
        log.debug("Successfully extracted member ID: {}", memberId);
    }

    /**
     * Journalise les tentatives d'extraction des scopes.
     */
    @Before("execution(* com.ecclesiaflow.business.security.AuthenticatedUserContextProvider.getAuthenticatedUserScopes())")
    public void logBeforeGetScopes(JoinPoint joinPoint) {
        log.debug("Extracting authenticated user scopes from JWT");
    }

    /**
     * Journalise le succès de l'extraction des scopes.
     */
    @AfterReturning(
        pointcut = "execution(* com.ecclesiaflow.business.security.AuthenticatedUserContextProvider.getAuthenticatedUserScopes())",
        returning = "scopes"
    )
    public void logAfterGetScopes(List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            log.warn("JWT scope claim is missing or empty");
        } else {
            log.debug("Successfully extracted {} scopes: {}", scopes.size(), scopes);
        }
    }

    /**
     * Journalise les erreurs lors de l'extraction du memberId.
     */
    @AfterThrowing(
        pointcut = "execution(* com.ecclesiaflow.business.security.AuthenticatedUserContextProvider.getAuthenticatedMemberId())",
        throwing = "exception"
    )
    public void logErrorGetMemberId(JoinPoint joinPoint, Exception exception) {
        if (exception.getMessage() != null && exception.getMessage().contains("missing 'cid' claim")) {
            log.error("JWT claim 'cid' (memberId) is missing");
        } else if (exception.getMessage() != null && exception.getMessage().contains("not a valid UUID")) {
            log.error("Invalid JWT 'cid' format: {}", exception.getCause() != null ? exception.getCause().getMessage() : "unknown");
        } else {
            log.error("Failed to extract member ID from JWT: {}", exception.getMessage());
        }
    }

    /**
     * Journalise les erreurs lors de l'extraction des scopes.
     */
    @AfterThrowing(
        pointcut = "execution(* com.ecclesiaflow.business.security.AuthenticatedUserContextProvider.getAuthenticatedUserScopes())",
        throwing = "exception"
    )
    public void logErrorGetScopes(JoinPoint joinPoint, Exception exception) {
        log.error("Failed to extract scopes from JWT: {}", exception.getMessage());
    }

    /**
     * Journalise les erreurs de parsing JWT.
     */
    @AfterThrowing(
        pointcut = "execution(* com.ecclesiaflow.business.security.AuthenticatedUserContextProvider.extractClaims())",
        throwing = "exception"
    )
    public void logErrorParseJwt(JoinPoint joinPoint, Exception exception) {
        log.error("Failed to parse JWT token: {}", exception.getMessage());
    }

    /**
     * Journalise les erreurs d'extraction du token depuis la requête HTTP.
     */
    @AfterThrowing(
        pointcut = "execution(* com.ecclesiaflow.business.security.AuthenticatedUserContextProvider.extractTokenFromRequest())",
        throwing = "exception"
    )
    public void logErrorExtractToken(JoinPoint joinPoint, Exception exception) {
        if (exception.getMessage() != null && exception.getMessage().contains("No HTTP request context")) {
            log.error("No request context available");
        } else if (exception.getMessage() != null && exception.getMessage().contains("Authorization header")) {
            log.error("Missing or invalid Authorization header");
        } else {
            log.error("Failed to extract token from request: {}", exception.getMessage());
        }
    }
}

package com.ecclesiaflow.business.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Aspect AOP pour la validation automatique des scopes (permissions).
 * <p>
 * Cet aspect intercepte toutes les méthodes annotées avec {@link RequireScopes}
 * et valide automatiquement que l'utilisateur authentifié possède les permissions nécessaires.
 * </p>
 * 
 * <p><strong>Flux d'exécution :</strong></p>
 * <ol>
 *   <li>Interception de la méthode annotée {@code @RequireScopes}</li>
 *   <li>Extraction des scopes requis depuis l'annotation</li>
 *   <li>Extraction des scopes de l'utilisateur depuis le JWT</li>
 *   <li>Validation via {@link ScopeValidator}</li>
 *   <li>Si validation OK → méthode exécutée</li>
 *   <li>Si validation KO → {@link com.ecclesiaflow.business.exceptions.InsufficientPermissionsException}</li>
 * </ol>
 * 
 * <p><strong>Exemple d'utilisation :</strong></p>
 * <pre>
 * {@code @RequireScopes({"ef:members:read:own", "ef:members:read:all"})}
 * public ResponseEntity&lt;Member&gt; getMember(UUID id) {
 *     // L'aspect valide automatiquement les scopes AVANT l'exécution
 *     return memberService.findById(id);
 * }
 * </pre>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see RequireScopes
 * @see ScopeValidator
 * @see AuthenticatedUserContextProvider
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ScopeValidationAspect {

    private final ScopeValidator scopeValidator;
    private final AuthenticatedUserContextProvider contextProvider;

    /**
     * Intercepte toutes les méthodes annotées avec {@link RequireScopes}
     * et valide les permissions AVANT l'exécution de la méthode.
     * 
     * @param joinPoint point d'interception AOP
     * @throws com.ecclesiaflow.business.exceptions.InsufficientPermissionsException si permissions insuffisantes
     */
    @Before("@annotation(com.ecclesiaflow.business.security.RequireScopes)")
    public void validateScopes(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        RequireScopes annotation = method.getAnnotation(RequireScopes.class);
        
        if (annotation == null) {
            log.warn("@RequireScopes annotation not found on method: {}", method.getName());
            return;
        }

        String[] requiredScopes = annotation.value();
        boolean requireAll = annotation.requireAll();

        log.debug("Validating scopes for method: {} - Required scopes: {}, Require all: {}", 
                method.getName(), requiredScopes, requireAll);

        // Extraction des scopes de l'utilisateur authentifié
        List<String> userScopes = contextProvider.getAuthenticatedUserScopes();
        
        log.debug("User scopes extracted from JWT: {}", userScopes);

        // Validation des scopes
        scopeValidator.validateScopes(userScopes, requiredScopes, requireAll);
        
        log.debug("Scope validation successful for method: {}", method.getName());
    }
}

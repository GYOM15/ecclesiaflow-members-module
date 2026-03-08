package com.ecclesiaflow.business.security;

import com.ecclesiaflow.web.security.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * @see AuthenticatedUserService
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ScopeValidationAspect {

    private final ScopeValidator scopeValidator;
    private final AuthenticatedUserService authenticatedUserService;
    private final RoleToScopeMapper roleToScopeMapper;

    @Value("${ecclesiaflow.scopes.enabled:true}")
    private boolean scopesEnabled;

    /**
     * Intercepte toutes les méthodes annotées avec {@link RequireScopes}
     * et valide les permissions AVANT l'exécution de la méthode.
     * <p>
     * La validation peut être désactivée via la propriété
     * {@code ecclesiaflow.scopes.enabled=false} (utile en développement
     * lorsque les scopes custom ne sont pas configurés dans Keycloak).
     * </p>
     *
     * @param joinPoint point d'interception AOP
     * @throws com.ecclesiaflow.business.exceptions.InsufficientPermissionsException si permissions insuffisantes
     */
    @Before("@annotation(com.ecclesiaflow.business.security.RequireScopes)")
    public void validateScopes(JoinPoint joinPoint) {
        if (!scopesEnabled) {
            log.debug("Scope validation is disabled (ecclesiaflow.scopes.enabled=false)");
            return;
        }

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

        // Merge JWT scopes (claim 'scope') with role-derived scopes
        Set<String> jwtScopes = authenticatedUserService.getScopes();
        Set<String> roleScopes = roleToScopeMapper.getScopesForRoles(
                authenticatedUserService.getRoles());

        Set<String> allScopes = new HashSet<>(jwtScopes);
        allScopes.addAll(roleScopes);

        List<String> userScopes = new ArrayList<>(allScopes);

        log.debug("User scopes (JWT + role-derived): {}", userScopes);

        scopeValidator.validateScopes(userScopes, requiredScopes, requireAll);

        log.debug("Scope validation successful for method: {}", method.getName());
    }
}

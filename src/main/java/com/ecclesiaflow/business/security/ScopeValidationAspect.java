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
 * AOP aspect that enforces {@link RequireScopes} annotations.
 *
 * <p>Merges JWT scopes with role-derived scopes (via {@link RoleToScopeMapper})
 * before delegating to {@link ScopeValidator}. Can be disabled with
 * {@code ecclesiaflow.scopes.enabled=false}.</p>
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

    /** Validates that the authenticated user holds the required scopes before method execution. */
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

package com.ecclesiaflow.business.security;

import com.ecclesiaflow.business.exceptions.InsufficientPermissionsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Validateur de scopes pour la sécurité basée sur les permissions.
 * <p>
 * Ce composant valide que l'utilisateur authentifié possède les scopes (permissions)
 * nécessaires pour accéder à une ressource ou exécuter une action.
 * </p>
 * 
 * <p><strong>Logique de validation :</strong></p>
 * <ul>
 *   <li><strong>OR (par défaut) :</strong> L'utilisateur doit avoir AU MOINS UN des scopes requis</li>
 *   <li><strong>AND :</strong> L'utilisateur doit avoir TOUS les scopes requis</li>
 * </ul>
 * 
 * <p><strong>Exemples :</strong></p>
 * <pre>
 * // OR : L'utilisateur doit avoir "read:own" OU "read:all"
 * validator.validateScopes(userScopes, new String[]{"ef:members:read:own", "ef:members:read:all"}, false);
 * 
 * // AND : L'utilisateur doit avoir "write:all" ET "admin"
 * validator.validateScopes(userScopes, new String[]{"ef:members:write:all", "ef:admin"}, true);
 * </pre>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see RequireScopes
 * @see ScopeValidationAspect
 */
@Component
@Slf4j
public class ScopeValidator {

    /**
     * Valide que l'utilisateur possède les scopes requis.
     * 
     * @param userScopes scopes de l'utilisateur authentifié (extraits du JWT)
     * @param requiredScopes scopes requis pour l'opération
     * @param requireAll {@code true} pour exiger tous les scopes (AND), {@code false} pour au moins un (OR)
     * @throws InsufficientPermissionsException si l'utilisateur n'a pas les permissions nécessaires
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    public void validateScopes(List<String> userScopes, String[] requiredScopes, boolean requireAll) {
        // Validation des paramètres
        if (userScopes == null) {
            log.error("User scopes cannot be null");
            throw new IllegalArgumentException("User scopes cannot be null");
        }
        
        if (requiredScopes == null || requiredScopes.length == 0) {
            log.error("Required scopes cannot be null or empty");
            throw new IllegalArgumentException("Required scopes cannot be null or empty");
        }

        log.debug("Validating scopes - User scopes: {}, Required scopes: {}, Require all: {}", 
                userScopes, Arrays.toString(requiredScopes), requireAll);

        // Validation selon la logique AND ou OR
        boolean hasPermission = requireAll 
            ? validateAllScopes(userScopes, requiredScopes)
            : validateAnyScope(userScopes, requiredScopes);

        if (!hasPermission) {
            String logic = requireAll ? "ALL" : "ANY";
            log.warn("Insufficient permissions - User scopes: {}, Required scopes ({}): {}", 
                    userScopes, logic, Arrays.toString(requiredScopes));
            throw new InsufficientPermissionsException(
                String.format("Insufficient permissions. Required scopes (%s): %s", 
                    logic, Arrays.toString(requiredScopes))
            );
        }

        log.debug("Scope validation successful");
    }

    /**
     * Vérifie que l'utilisateur possède TOUS les scopes requis (logique AND).
     * 
     * @param userScopes scopes de l'utilisateur
     * @param requiredScopes scopes requis
     * @return {@code true} si l'utilisateur a tous les scopes, {@code false} sinon
     */
    private boolean validateAllScopes(List<String> userScopes, String[] requiredScopes) {
        return Arrays.stream(requiredScopes)
                .allMatch(userScopes::contains);
    }

    /**
     * Vérifie que l'utilisateur possède AU MOINS UN des scopes requis (logique OR).
     * 
     * @param userScopes scopes de l'utilisateur
     * @param requiredScopes scopes requis
     * @return {@code true} si l'utilisateur a au moins un scope, {@code false} sinon
     */
    private boolean validateAnyScope(List<String> userScopes, String[] requiredScopes) {
        return Arrays.stream(requiredScopes)
                .anyMatch(userScopes::contains);
    }
}

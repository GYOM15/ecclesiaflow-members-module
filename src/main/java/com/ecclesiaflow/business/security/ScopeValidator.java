package com.ecclesiaflow.business.security;

import com.ecclesiaflow.business.exceptions.InsufficientPermissionsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Validates that the authenticated user holds the required scopes.
 * Supports AND ({@code requireAll = true}) and OR ({@code requireAll = false}) logic.
 */
@Component
@Slf4j
public class ScopeValidator {

    /**
     * Validates user scopes against required scopes.
     *
     * @throws InsufficientPermissionsException if the user lacks the required scopes
     */
    public void validateScopes(List<String> userScopes, String[] requiredScopes, boolean requireAll) {
        if (userScopes == null) {
            throw new IllegalArgumentException("User scopes cannot be null");
        }
        if (requiredScopes == null || requiredScopes.length == 0) {
            throw new IllegalArgumentException("Required scopes cannot be null or empty");
        }

        log.debug("Validating scopes - User: {}, Required: {}, Require all: {}",
                userScopes, Arrays.toString(requiredScopes), requireAll);

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

    private boolean validateAllScopes(List<String> userScopes, String[] requiredScopes) {
        return Arrays.stream(requiredScopes).allMatch(userScopes::contains);
    }

    private boolean validateAnyScope(List<String> userScopes, String[] requiredScopes) {
        return Arrays.stream(requiredScopes).anyMatch(userScopes::contains);
    }
}

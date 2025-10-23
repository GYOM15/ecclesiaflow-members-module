package com.ecclesiaflow.business.security;

import com.ecclesiaflow.business.exceptions.InsufficientPermissionsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests unitaires pour {@link ScopeValidator}.
 * <p>
 * Vérifie la logique de validation des scopes (AND et OR).
 * </p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
class ScopeValidatorTest {

    private ScopeValidator scopeValidator;

    @BeforeEach
    void setUp() {
        scopeValidator = new ScopeValidator();
    }

    // ========================================
    // Tests de validation OR (au moins un scope)
    // ========================================

    @Test
    void validateScopes_shouldPass_whenUserHasOneOfRequiredScopes_OR() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:read:own", "ef:members:write:own");
        String[] requiredScopes = {"ef:members:read:own", "ef:members:read:all"};
        boolean requireAll = false;

        // When & Then
        assertThatCode(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .doesNotThrowAnyException();
    }

    @Test
    void validateScopes_shouldPass_whenUserHasAllRequiredScopes_OR() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:read:own", "ef:members:read:all");
        String[] requiredScopes = {"ef:members:read:own", "ef:members:read:all"};
        boolean requireAll = false;

        // When & Then
        assertThatCode(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .doesNotThrowAnyException();
    }

    @Test
    void validateScopes_shouldFail_whenUserHasNoneOfRequiredScopes_OR() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:write:own");
        String[] requiredScopes = {"ef:members:read:own", "ef:members:read:all"};
        boolean requireAll = false;

        // When & Then
        assertThatThrownBy(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .isInstanceOf(InsufficientPermissionsException.class)
                .hasMessageContaining("Insufficient permissions")
                .hasMessageContaining("ANY");
    }

    @Test
    void validateScopes_shouldFail_whenUserScopesAreEmpty_OR() {
        // Given
        List<String> userScopes = Collections.emptyList();
        String[] requiredScopes = {"ef:members:read:own"};
        boolean requireAll = false;

        // When & Then
        assertThatThrownBy(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .isInstanceOf(InsufficientPermissionsException.class);
    }

    // ========================================
    // Tests de validation AND (tous les scopes)
    // ========================================

    @Test
    void validateScopes_shouldPass_whenUserHasAllRequiredScopes_AND() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:write:all", "ef:admin", "ef:members:read:all");
        String[] requiredScopes = {"ef:members:write:all", "ef:admin"};
        boolean requireAll = true;

        // When & Then
        assertThatCode(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .doesNotThrowAnyException();
    }

    @Test
    void validateScopes_shouldFail_whenUserMissesOneScope_AND() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:write:all");
        String[] requiredScopes = {"ef:members:write:all", "ef:admin"};
        boolean requireAll = true;

        // When & Then
        assertThatThrownBy(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .isInstanceOf(InsufficientPermissionsException.class)
                .hasMessageContaining("Insufficient permissions")
                .hasMessageContaining("ALL");
    }

    @Test
    void validateScopes_shouldFail_whenUserHasNoneOfRequiredScopes_AND() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:read:own");
        String[] requiredScopes = {"ef:members:write:all", "ef:admin"};
        boolean requireAll = true;

        // When & Then
        assertThatThrownBy(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .isInstanceOf(InsufficientPermissionsException.class);
    }

    // ========================================
    // Tests de validation des paramètres
    // ========================================

    @Test
    void validateScopes_shouldThrowException_whenUserScopesAreNull() {
        // Given
        List<String> userScopes = null;
        String[] requiredScopes = {"ef:members:read:own"};
        boolean requireAll = false;

        // When & Then
        assertThatThrownBy(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User scopes cannot be null");
    }

    @Test
    void validateScopes_shouldThrowException_whenRequiredScopesAreNull() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:read:own");
        String[] requiredScopes = null;
        boolean requireAll = false;

        // When & Then
        assertThatThrownBy(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Required scopes cannot be null or empty");
    }

    @Test
    void validateScopes_shouldThrowException_whenRequiredScopesAreEmpty() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:read:own");
        String[] requiredScopes = new String[0];
        boolean requireAll = false;

        // When & Then
        assertThatThrownBy(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Required scopes cannot be null or empty");
    }

    // ========================================
    // Tests de cas limites
    // ========================================

    @Test
    void validateScopes_shouldPass_whenSingleScopeMatches_OR() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:read:own");
        String[] requiredScopes = {"ef:members:read:own"};
        boolean requireAll = false;

        // When & Then
        assertThatCode(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .doesNotThrowAnyException();
    }

    @Test
    void validateScopes_shouldPass_whenSingleScopeMatches_AND() {
        // Given
        List<String> userScopes = Arrays.asList("ef:members:read:own");
        String[] requiredScopes = {"ef:members:read:own"};
        boolean requireAll = true;

        // When & Then
        assertThatCode(() -> scopeValidator.validateScopes(userScopes, requiredScopes, requireAll))
                .doesNotThrowAnyException();
    }
}

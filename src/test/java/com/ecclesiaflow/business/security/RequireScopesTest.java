package com.ecclesiaflow.business.security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour l'annotation {@link RequireScopes}.
 * <p>
 * Vérifie que l'annotation est correctement configurée et que ses métadonnées
 * sont accessibles via réflexion.
 * </p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
class RequireScopesTest {

    /**
     * Classe de test avec méthodes annotées pour valider le comportement de {@link RequireScopes}.
     */
    static class TestClass {
        
        @RequireScopes({"ef:members:read:own"})
        public void singleScope() {}
        
        @RequireScopes({"ef:members:read:own", "ef:members:read:all"})
        public void multipleScopes() {}
        
        @RequireScopes(value = {"ef:members:write:all", "ef:admin"}, requireAll = true)
        public void requireAllScopes() {}
        
        @RequireScopes(value = {"ef:members:delete:own"}, requireAll = false)
        public void requireAnyScope() {}
    }

    @Test
    void annotation_shouldBePresent_onAnnotatedMethod() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("singleScope");
        
        // When
        boolean isPresent = method.isAnnotationPresent(RequireScopes.class);
        
        // Then
        assertThat(isPresent).isTrue();
    }

    @Test
    void annotation_shouldReturnCorrectScopes_forSingleScope() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("singleScope");
        
        // When
        RequireScopes annotation = method.getAnnotation(RequireScopes.class);
        
        // Then
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactly("ef:members:read:own");
        assertThat(annotation.requireAll()).isFalse(); // Valeur par défaut
    }

    @Test
    void annotation_shouldReturnCorrectScopes_forMultipleScopes() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("multipleScopes");
        
        // When
        RequireScopes annotation = method.getAnnotation(RequireScopes.class);
        
        // Then
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactly("ef:members:read:own", "ef:members:read:all");
        assertThat(annotation.requireAll()).isFalse();
    }

    @Test
    void annotation_shouldReturnTrue_whenRequireAllIsSet() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("requireAllScopes");
        
        // When
        RequireScopes annotation = method.getAnnotation(RequireScopes.class);
        
        // Then
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactly("ef:members:write:all", "ef:admin");
        assertThat(annotation.requireAll()).isTrue();
    }

    @Test
    void annotation_shouldReturnFalse_whenRequireAllIsExplicitlySetToFalse() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("requireAnyScope");
        
        // When
        RequireScopes annotation = method.getAnnotation(RequireScopes.class);
        
        // Then
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).containsExactly("ef:members:delete:own");
        assertThat(annotation.requireAll()).isFalse();
    }

    @Test
    void annotation_shouldNotBePresent_onNonAnnotatedMethod() throws NoSuchMethodException {
        // Given
        Method method = Object.class.getMethod("toString");
        
        // When
        boolean isPresent = method.isAnnotationPresent(RequireScopes.class);
        
        // Then
        assertThat(isPresent).isFalse();
    }
}

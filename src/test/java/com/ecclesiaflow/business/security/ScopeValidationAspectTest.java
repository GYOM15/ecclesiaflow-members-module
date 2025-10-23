package com.ecclesiaflow.business.security;

import com.ecclesiaflow.business.exceptions.InsufficientPermissionsException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ScopeValidationAspect.
 */
@ExtendWith(MockitoExtension.class)
class ScopeValidationAspectTest {

    @Mock
    private ScopeValidator scopeValidator;

    @Mock
    private AuthenticatedUserContextProvider contextProvider;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private ScopeValidationAspect aspect;

    @BeforeEach
    void setUp() {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @Test
    void validateScopes_shouldValidateSuccessfully_whenUserHasRequiredScopes() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithScopes");
        when(methodSignature.getMethod()).thenReturn(method);
        when(contextProvider.getAuthenticatedUserScopes())
                .thenReturn(List.of("ef:members:read:own", "ef:members:write:own"));

        // When & Then
        assertThatCode(() -> aspect.validateScopes(joinPoint))
                .doesNotThrowAnyException();

        verify(scopeValidator).validateScopes(
                List.of("ef:members:read:own", "ef:members:write:own"),
                new String[]{"ef:members:read:own"},
                false
        );
    }

    @Test
    void validateScopes_shouldThrowException_whenUserLacksRequiredScopes() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithScopes");
        when(methodSignature.getMethod()).thenReturn(method);
        when(contextProvider.getAuthenticatedUserScopes())
                .thenReturn(List.of("ef:members:write:own"));

        doThrow(new InsufficientPermissionsException("Permissions insuffisantes"))
                .when(scopeValidator).validateScopes(any(), any(), anyBoolean());

        // When & Then
        assertThatThrownBy(() -> aspect.validateScopes(joinPoint))
                .isInstanceOf(InsufficientPermissionsException.class)
                .hasMessageContaining("Permissions insuffisantes");
    }

    @Test
    void validateScopes_shouldHandleRequireAll_whenAnnotationSpecifiesIt() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithRequireAll");
        when(methodSignature.getMethod()).thenReturn(method);
        when(contextProvider.getAuthenticatedUserScopes())
                .thenReturn(List.of("ef:members:read:own", "ef:members:write:own"));

        // When
        aspect.validateScopes(joinPoint);

        // Then
        verify(scopeValidator).validateScopes(
                List.of("ef:members:read:own", "ef:members:write:own"),
                new String[]{"ef:members:read:own", "ef:members:write:own"},
                true
        );
    }

    @Test
    void validateScopes_shouldReturnEarly_whenAnnotationNotFound() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithoutAnnotation");
        when(methodSignature.getMethod()).thenReturn(method);

        // When
        aspect.validateScopes(joinPoint);

        // Then
        verify(scopeValidator, never()).validateScopes(any(), any(), anyBoolean());
        verify(contextProvider, never()).getAuthenticatedUserScopes();
    }

    // Test class with annotated methods
    static class TestClass {
        @RequireScopes("ef:members:read:own")
        public void methodWithScopes() {
        }

        @RequireScopes(value = {"ef:members:read:own", "ef:members:write:own"}, requireAll = true)
        public void methodWithRequireAll() {
        }

        public void methodWithoutAnnotation() {
        }
    }
}

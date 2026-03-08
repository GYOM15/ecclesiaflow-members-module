package com.ecclesiaflow.business.security;

import com.ecclesiaflow.business.exceptions.InsufficientPermissionsException;
import com.ecclesiaflow.web.security.AuthenticatedUserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScopeValidationAspectTest {

    @Mock
    private ScopeValidator scopeValidator;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private RoleToScopeMapper roleToScopeMapper;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private ScopeValidationAspect aspect;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aspect, "scopesEnabled", true);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @Test
    void validateScopes_shouldValidateSuccessfully_whenUserHasRequiredScopes() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithScopes");
        when(methodSignature.getMethod()).thenReturn(method);
        when(authenticatedUserService.getScopes()).thenReturn(Set.of("ef:members:read:own"));
        when(authenticatedUserService.getRoles()).thenReturn(Set.of("USER"));
        when(roleToScopeMapper.getScopesForRoles(Set.of("USER")))
                .thenReturn(Set.of("ef:members:read:own", "ef:members:write:own", "ef:members:delete:own"));

        // When & Then
        assertThatCode(() -> aspect.validateScopes(joinPoint))
                .doesNotThrowAnyException();

        verify(scopeValidator).validateScopes(
                anyList(),
                eq(new String[]{"ef:members:read:own"}),
                eq(false)
        );
    }

    @Test
    void validateScopes_shouldThrowException_whenUserLacksRequiredScopes() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithScopes");
        when(methodSignature.getMethod()).thenReturn(method);
        when(authenticatedUserService.getScopes()).thenReturn(Set.of());
        when(authenticatedUserService.getRoles()).thenReturn(Set.of());
        when(roleToScopeMapper.getScopesForRoles(Set.of())).thenReturn(Set.of());

        doThrow(new InsufficientPermissionsException("Insufficient permissions"))
                .when(scopeValidator).validateScopes(any(), any(), anyBoolean());

        // When & Then
        assertThatThrownBy(() -> aspect.validateScopes(joinPoint))
                .isInstanceOf(InsufficientPermissionsException.class)
                .hasMessageContaining("Insufficient permissions");
    }

    @Test
    void validateScopes_shouldHandleRequireAll_whenAnnotationSpecifiesIt() throws NoSuchMethodException {
        // Given
        Method method = TestClass.class.getMethod("methodWithRequireAll");
        when(methodSignature.getMethod()).thenReturn(method);
        when(authenticatedUserService.getScopes())
                .thenReturn(Set.of("ef:members:read:own", "ef:members:write:own"));
        when(authenticatedUserService.getRoles()).thenReturn(Set.of("USER"));
        when(roleToScopeMapper.getScopesForRoles(Set.of("USER")))
                .thenReturn(Set.of("ef:members:read:own", "ef:members:write:own", "ef:members:delete:own"));

        // When
        aspect.validateScopes(joinPoint);

        // Then
        verify(scopeValidator).validateScopes(
                anyList(),
                eq(new String[]{"ef:members:read:own", "ef:members:write:own"}),
                eq(true)
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
        verifyNoInteractions(authenticatedUserService, roleToScopeMapper);
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

package com.ecclesiaflow.web.security;

import com.ecclesiaflow.web.client.AuthClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour l'implémentation JWT de AuthenticationService.
 * Vérifie que la délégation vers AuthClient est correcte et gère les cas limites.
 * Prend en compte le comportement de AuthClient qui encapsule les exceptions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Jwt (AuthenticationService Impl) - Tests Unitaires")
class JwtTest {

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private Jwt jwtService;

    private static final String AUTH_CLIENT_FALLBACK_TOKEN = "temporary-token-mock-for-dev";
    private static final UUID TEST_MEMBER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    @DisplayName("Devrait générer un token temporaire en déléguant à AuthClient")
    void retrievePostActivationToken_WithValidEmail_ShouldDelegateToAuthClient() {
        // Given
        String email = "test@example.com";
        UUID memberId = TEST_MEMBER_ID;
        String expectedToken = "mocked_jwt_token_123";

        when(authClient.retrievePostActivationToken(email, memberId)).thenReturn(expectedToken);

        // When
        String actualToken = jwtService.retrievePostActivationToken(email, memberId);

        // Then
        assertThat(actualToken).isEqualTo(expectedToken);

        verify(authClient, times(1)).retrievePostActivationToken(email, memberId);
        verifyNoMoreInteractions(authClient);
    }

    @Test
    @DisplayName("Devrait gérer un email vide en le passant à AuthClient")
    void retrievePostActivationToken_WithEmptyEmail_ShouldPassToAuthClient() {
        // Given
        String email = "";
        UUID memberId = TEST_MEMBER_ID;
        String expectedToken = "token_for_empty_email";
        when(authClient.retrievePostActivationToken(email, memberId)).thenReturn(expectedToken);

        // When
        String actualToken = jwtService.retrievePostActivationToken(email, memberId);

        // Then
        assertThat(actualToken).isEqualTo(expectedToken);
        verify(authClient, times(1)).retrievePostActivationToken(email, memberId);
        verifyNoMoreInteractions(authClient);
    }

    @Test
    @DisplayName("Devrait gérer un email null en le passant à AuthClient")
    void retrievePostActivationToken_WithNullEmail_ShouldPassToAuthClient() {
        // Given
        String email = null;
        UUID memberId = TEST_MEMBER_ID;
        String expectedToken = "token_for_null_email";
        when(authClient.retrievePostActivationToken(email, memberId)).thenReturn(expectedToken);

        // When
        String actualToken = jwtService.retrievePostActivationToken(email, memberId);

        // Then
        assertThat(actualToken).isEqualTo(expectedToken);
        verify(authClient, times(1)).retrievePostActivationToken(email, memberId);
        verifyNoMoreInteractions(authClient);
    }

    @Test
    @DisplayName("Devrait retourner le token de fallback de AuthClient si AuthClient échoue")
    void retrievePostActivationToken_AuthClientFails_ShouldReturnAuthClientFallbackToken() {
        // Given
        String email = "failure@example.com";
        UUID memberId = TEST_MEMBER_ID;
        when(authClient.retrievePostActivationToken(email, memberId)).thenReturn(AUTH_CLIENT_FALLBACK_TOKEN);

        // When
        String actualToken = jwtService.retrievePostActivationToken(email, memberId);

        // Then
        assertThat(actualToken).isEqualTo(AUTH_CLIENT_FALLBACK_TOKEN);
        verify(authClient, times(1)).retrievePostActivationToken(email, memberId);
        verifyNoMoreInteractions(authClient);
    }
}
package com.ecclesiaflow.web.security;

import com.ecclesiaflow.web.client.AuthClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour l'implémentation JWT de TokenGenerator.
 * Vérifie que la délégation vers AuthClient est correcte et gère les cas limites.
 * Prend en compte le comportement de AuthClient qui encapsule les exceptions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Jwt (TokenGenerator Impl) - Tests Unitaires")
class JwtTest {

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private Jwt jwtService;

    private static final String AUTH_CLIENT_FALLBACK_TOKEN = "temporary-token-mock-for-dev";

    @Test
    @DisplayName("Devrait générer un token temporaire en déléguant à AuthClient")
    void generateTemporaryToken_WithValidEmail_ShouldDelegateToAuthClient() {
        // Given
        String email = "test@example.com";
        String expectedToken = "mocked_jwt_token_123";

        when(authClient.generateTemporaryToken(email)).thenReturn(expectedToken);

        // When
        String actualToken = jwtService.generateTemporaryToken(email);

        // Then
        assertThat(actualToken).isEqualTo(expectedToken);

        verify(authClient, times(1)).generateTemporaryToken(email);
        verifyNoMoreInteractions(authClient);
    }

    @Test
    @DisplayName("Devrait gérer un email vide en le passant à AuthClient")
    void generateTemporaryToken_WithEmptyEmail_ShouldPassToAuthClient() {
        // Given
        String email = "";
        String expectedToken = "token_for_empty_email";
        when(authClient.generateTemporaryToken(email)).thenReturn(expectedToken);

        // When
        String actualToken = jwtService.generateTemporaryToken(email);

        // Then
        assertThat(actualToken).isEqualTo(expectedToken);
        verify(authClient, times(1)).generateTemporaryToken(email);
        verifyNoMoreInteractions(authClient);
    }

    @Test
    @DisplayName("Devrait gérer un email null en le passant à AuthClient")
    void generateTemporaryToken_WithNullEmail_ShouldPassToAuthClient() {
        // Given
        String email = null;
        String expectedToken = "token_for_null_email";
        when(authClient.generateTemporaryToken(email)).thenReturn(expectedToken);

        // When
        String actualToken = jwtService.generateTemporaryToken(email);

        // Then
        assertThat(actualToken).isEqualTo(expectedToken);
        verify(authClient, times(1)).generateTemporaryToken(email);
        verifyNoMoreInteractions(authClient);
    }

    @Test
    @DisplayName("Devrait retourner le token de fallback de AuthClient si AuthClient échoue")
    void generateTemporaryToken_AuthClientFails_ShouldReturnAuthClientFallbackToken() {
        // Given
        String email = "failure@example.com";
        when(authClient.generateTemporaryToken(email)).thenReturn(AUTH_CLIENT_FALLBACK_TOKEN);

        // When
        String actualToken = jwtService.generateTemporaryToken(email);

        // Then
        assertThat(actualToken).isEqualTo(AUTH_CLIENT_FALLBACK_TOKEN);
        verify(authClient, times(1)).generateTemporaryToken(email);
        verifyNoMoreInteractions(authClient);
    }
}
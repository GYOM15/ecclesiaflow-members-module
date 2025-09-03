package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.web.client.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthClient.
 * Vérifie l'intégration avec le module d'authentification externe.
 */
@ExtendWith(MockitoExtension.class)
class AuthClientTest {

    @Mock
    private WebClient authWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        // Configuration des mocks pour WebClient
        when(authWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    void generateTemporaryToken_WithValidEmail_ShouldReturnToken() {
        // Given
        String email = "test@example.com";
        String expectedToken = "temp_token_123";
        Map<String, Object> mockResponse = Map.of("temporaryToken", expectedToken);
        
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockResponse));

        // When
        String result = authClient.generateTemporaryToken(email);

        // Then
        assertEquals(expectedToken, result);
        verify(authWebClient).post();
        verify(requestBodyUriSpec).uri("/ecclesiaflow/auth/temporary-token");
        verify(requestBodySpec).bodyValue(Map.of("email", email));
    }

    @Test
    void generateTemporaryToken_WithWebClientException_ShouldReturnMockToken() {
        // Given
        String email = "test@example.com";
        when(responseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.error(new WebClientResponseException(500, "Internal Server Error", null, null, null)));

        // When
        String result = authClient.generateTemporaryToken(email);

        // Then
        assertEquals("temporary-token-mock-for-dev", result);
    }

    @Test
    void generateTemporaryToken_WithNullResponse_ShouldReturnMockToken() {
        // Given
        String email = "test@example.com";
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of()));

        // When
        String result = authClient.generateTemporaryToken(email);

        // Then
        assertEquals("temporary-token-mock-for-dev", result);
    }

    @Test
    void generateTemporaryToken_WithEmptyEmail_ShouldStillCallService() {
        // Given
        String email = "";
        String expectedToken = "temp_token_empty";
        Map<String, Object> mockResponse = Map.of("temporaryToken", expectedToken);
        
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockResponse));

        // When
        String result = authClient.generateTemporaryToken(email);

        // Then
        assertEquals(expectedToken, result);
        verify(requestBodySpec).bodyValue(Map.of("email", email));
    }

    @Test
    void setPassword_WithValidData_ShouldCallAuthModule() {
        // Given
        String email = "test@example.com";
        String password = "newPassword123";
        String temporaryToken = "temp_token_123";
        
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // When
        authClient.setPassword(email, password, temporaryToken);

        // Then
        verify(authWebClient).post();
        verify(requestBodyUriSpec).uri("/ecclesiaflow/auth/set-password");
        verify(requestBodySpec).bodyValue(Map.of(
            "email", email,
            "password", password,
            "temporaryToken", temporaryToken
        ));
    }

    @Test
    void setPassword_WithWebClientException_ShouldNotThrowException() {
        // Given
        String email = "test@example.com";
        String password = "newPassword123";
        String temporaryToken = "temp_token_123";
        
        when(responseSpec.bodyToMono(Void.class))
            .thenReturn(Mono.error(new WebClientResponseException(400, "Bad Request", null, null, null)));

        // When & Then - Ne doit pas lever d'exception
        assertDoesNotThrow(() -> authClient.setPassword(email, password, temporaryToken));
    }

    @Test
    void setPassword_WithNullValues_ShouldStillCallService() {
        // Given
        String email = null;
        String password = null;
        String temporaryToken = null;

        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // When
        authClient.setPassword(email, password, temporaryToken);

        // Then
        Map<String, Object> expectedBody = new java.util.HashMap<>();
        expectedBody.put("email", email);
        expectedBody.put("password", password);
        expectedBody.put("temporaryToken", temporaryToken);

        verify(requestBodySpec).bodyValue(expectedBody);
    }

    @Test
    void changePassword_WithValidData_ShouldCallAuthModule() {
        // Given
        String email = "test@example.com";
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword456";
        
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // When
        authClient.changePassword(email, currentPassword, newPassword);

        // Then
        verify(authWebClient).post();
        verify(requestBodyUriSpec).uri("/ecclesiaflow/auth/change-password");
        verify(requestBodySpec).bodyValue(Map.of(
            "email", email,
            "currentPassword", currentPassword,
            "newPassword", newPassword
        ));
    }

    @Test
    void changePassword_WithWebClientException_ShouldThrowException() {
        // Given
        String email = "test@example.com";
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword456";
        
        WebClientResponseException exception = new WebClientResponseException(
            401, "Unauthorized", null, null, null);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.error(exception));

        // When & Then
        assertThrows(WebClientResponseException.class, () ->
            authClient.changePassword(email, currentPassword, newPassword));
    }

    @Test
    void changePassword_With4xxError_ShouldThrowException() {
        // Given
        String email = "test@example.com";
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword456";
        
        WebClientResponseException exception = new WebClientResponseException(
            400, "Bad Request", null, null, null);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.error(exception));

        // When & Then
        assertThrows(WebClientResponseException.class, () ->
            authClient.changePassword(email, currentPassword, newPassword));
    }

    @Test
    void changePassword_With5xxError_ShouldThrowException() {
        // Given
        String email = "test@example.com";
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword456";
        
        WebClientResponseException exception = new WebClientResponseException(
            500, "Internal Server Error", null, null, null);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.error(exception));

        // When & Then
        assertThrows(WebClientResponseException.class, () ->
            authClient.changePassword(email, currentPassword, newPassword));
    }
}

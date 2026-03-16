package com.ecclesiaflow.web.client;

import com.ecclesiaflow.business.domain.auth.PasswordSetupTokenResponse;
import com.ecclesiaflow.web.model.TemporaryTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthClientImplTest {

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
    private AuthClientImpl authClient;

    private static final String EMAIL = "test@example.com";
    private static final UUID MEMBER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String GENERATED_TOKEN = "temporary-token-mock-for-dev";
    private static final String FALLBACK_TOKEN = "temporary-token-mock-for-dev";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(authWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(java.util.function.Predicate.class), any(java.util.function.Function.class))).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("should return PasswordSetupTokenResponse on successful API call")
    void getTempToken_ForActivatedMember_success() {
        // Arrange
        TemporaryTokenResponse apiResponse = new TemporaryTokenResponse(GENERATED_TOKEN);
        apiResponse.setExpiresInSeconds(900);
        apiResponse.setPasswordEndpoint("/ecclesiaflow/auth/password/setup");

        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(apiResponse));

        // Act
        PasswordSetupTokenResponse result = authClient.retrievePostActivationToken(EMAIL, MEMBER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(GENERATED_TOKEN);
        assertThat(result.expiresInSeconds()).isEqualTo(900);
        assertThat(result.passwordEndpoint()).isEqualTo("/ecclesiaflow/auth/password/setup");
    }

    @Test
    @DisplayName("should return fallback response on 4xx client error")
    void getTempToken_ForActivatedMember_clientError() {
        // Arrange
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.error(new RuntimeException("Simulated 400 error")));

        // Act
        PasswordSetupTokenResponse result = authClient.retrievePostActivationToken(EMAIL, MEMBER_ID);

        // Assert — fallback response
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(FALLBACK_TOKEN);
    }

    @Test
    @DisplayName("should return fallback response on 5xx server error")
    void getTempToken_ForActivatedMember_serverError() {
        // Arrange
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.error(new RuntimeException("Simulated 500 error")));

        // Act
        PasswordSetupTokenResponse result = authClient.retrievePostActivationToken(EMAIL, MEMBER_ID);

        // Assert — fallback response
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(FALLBACK_TOKEN);
    }

    @Test
    @DisplayName("should return fallback response on network connectivity error")
    void getTempToken_ForActivatedMember_networkError() {
        // Arrange
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.error(new WebClientRequestException(
                        new ConnectException("Connection refused"),
                        org.springframework.http.HttpMethod.POST,
                        java.net.URI.create("http://localhost:8080/ecclesiaflow/auth/temporary-token"),
                        org.springframework.http.HttpHeaders.EMPTY
                )));

        // Act
        PasswordSetupTokenResponse result = authClient.retrievePostActivationToken(EMAIL, MEMBER_ID);

        // Assert — fallback response
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(FALLBACK_TOKEN);
    }

    @Test
    @DisplayName("should return fallback response when API response triggers exception")
    void getTempToken_missingTokenForActivatedMemberInResponse() {
        // Arrange — null response will cause NPE → caught → fallback
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.empty());

        // Act
        PasswordSetupTokenResponse result = authClient.retrievePostActivationToken(EMAIL, MEMBER_ID);

        // Assert — fallback response
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(FALLBACK_TOKEN);
    }
}

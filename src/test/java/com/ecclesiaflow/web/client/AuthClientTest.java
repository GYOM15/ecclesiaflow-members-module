package com.ecclesiaflow.web.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

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

    private static final String EMAIL = "test@example.com";
    private static final String TEMPORARY_TOKEN_MOCK = "temporary-token-mock-for-dev";
    private static final String GENERATED_TOKEN = "actual-generated-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(authWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(anyMap())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(java.util.function.Predicate.class), any(java.util.function.Function.class))).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("should return generated token on successful API call")
    void generateTemporaryToken_success() {
        // Arrange: Mock the final bodyToMono to return a successful response map
        Map<String, String> successResponse = Map.of("temporaryToken", GENERATED_TOKEN);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(successResponse));

        // Act
        String result = authClient.generateTemporaryToken(EMAIL);

        // Assert
        assertThat(result).isEqualTo(GENERATED_TOKEN);
    }

    @Test
    @DisplayName("should return mock token on 4xx client error")
    void generateTemporaryToken_clientError() {
        // Arrange: Mock bodyToMono to return an error when a 4xx status is encountered
        when(responseSpec.onStatus(any(java.util.function.Predicate.class), any(java.util.function.Function.class)))
                .thenAnswer(invocation -> {
                    Function<ClientResponse, Mono<? extends Throwable>> errorHandler = invocation.getArgument(1);
                    // Simulate a 400 Bad Request
                    ClientResponse mockClientResponse = ClientResponse.create(HttpStatus.BAD_REQUEST).build();
                    return responseSpec.bodyToMono(Map.class).then(errorHandler.apply(mockClientResponse));
                });
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.error(new RuntimeException("Simulated 400 error")));

        // Act
        String result = authClient.generateTemporaryToken(EMAIL);

        // Assert
        assertThat(result).isEqualTo(TEMPORARY_TOKEN_MOCK);
    }

    @Test
    @DisplayName("should return mock token on 5xx server error")
    void generateTemporaryToken_serverError() {
        // Arrange: Mock bodyToMono to return an error when a 5xx status is encountered
        when(responseSpec.onStatus(any(java.util.function.Predicate.class), any(java.util.function.Function.class)))
                .thenAnswer(invocation -> {
                    Function<ClientResponse, Mono<? extends Throwable>> errorHandler = invocation.getArgument(1);
                    // Simulate a 500 Internal Server Error
                    ClientResponse mockClientResponse = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    return responseSpec.bodyToMono(Map.class).then(errorHandler.apply(mockClientResponse));
                });
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.error(new RuntimeException("Simulated 500 error")));

        // Act
        String result = authClient.generateTemporaryToken(EMAIL);

        // Assert
        assertThat(result).isEqualTo(TEMPORARY_TOKEN_MOCK);
    }

    @Test
    @DisplayName("should return mock token on network connectivity error")
    void generateTemporaryToken_networkError() {
        // Arrange: Simulate a network error during bodyToMono()
        when(responseSpec.bodyToMono(any(Class.class)))
                .thenReturn(Mono.error(new WebClientRequestException(
                        new ConnectException("Connection refused"),
                        org.springframework.http.HttpMethod.POST,
                        java.net.URI.create("http://localhost:8080/ecclesiaflow/auth/temporary-token"),
                        org.springframework.http.HttpHeaders.EMPTY
                )));

        // Act
        String result = authClient.generateTemporaryToken(EMAIL);

        // Assert
        assertThat(result).isEqualTo(TEMPORARY_TOKEN_MOCK);
    }

    @Test
    @DisplayName("should return mock token if the response map does not contain 'temporaryToken'")
    void generateTemporaryToken_missingTokenInResponse() {
        // Arrange: Mock the final bodyToMono to return a response map without the expected key
        Map<String, String> malformedResponse = Map.of("someOtherKey", "someValue");
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(malformedResponse));

        // Act
        String result = authClient.generateTemporaryToken(EMAIL);

        // Assert
        assertThat(result).isEqualTo(TEMPORARY_TOKEN_MOCK);
    }
}
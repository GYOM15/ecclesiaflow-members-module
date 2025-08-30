package com.ecclesiaflow.business.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ecclesiaflow.auth.module.enabled", havingValue = "true", matchIfMissing = true)
public class AuthModuleService {

    private final WebClient authWebClient;

    public String generateTemporaryToken(String email) {
        try {
            return post("/ecclesiaflow/auth/temporary-token", Map.of("email", email))
                    .map(response -> (String) response.get("temporaryToken"))
                    .block();
        } catch (Exception e) {
            return "temporary-token-mock-for-dev"; // Valeur de test en développement
        }
    }

    public void setPassword(String email, String password, String temporaryToken) {
        try {
            postVoid("/ecclesiaflow/auth/set-password", Map.of(
                    "email", email,
                    "password", password,
                    "temporaryToken", temporaryToken
            )).block();
        } catch (Exception e) {}
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        postVoid("/ecclesiaflow/auth/change-password", Map.of(
                "email", email,
                "currentPassword", currentPassword,
                "newPassword", newPassword
        )).block();
    }

    // === Utility Methods ===

    private Mono<Map> post(String path, Map<String, String> body) {
        return authWebClient
                .post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.createException().flatMap(Mono::error)
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.createException().flatMap(Mono::error)
                )
                .bodyToMono(Map.class);
    }

    private Mono<Void> postVoid(String path, Map<String, String> body) {
        return authWebClient
                .post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.createException().flatMap(Mono::error)
                )
                .bodyToMono(Void.class);
    }
}
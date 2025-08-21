package com.ecclesiaflow.business.services.impl;

import com.ecclesiaflow.business.services.AuthModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthModuleServiceImpl implements AuthModuleService {

    private final RestTemplate restTemplate;

    @Value("${ecclesiaflow.auth.module.base-url:http://localhost:8081}")
    private String authModuleBaseUrl;

    @Override
    public String generateTemporaryToken(String email) {
            Map<String, String> request = Map.of("email", email);
            Map<String, Object> response = postForMapResponse("/ecclesiaflow/auth/temporary-token", request);
        return (String) response.get("temporaryToken");
    }


    // -------------------------------
    // Méthodes utilitaires privées
    // -------------------------------

    private Map<String, Object> postForMapResponse(String path, Map<String, String> request) {
        String url = authModuleBaseUrl + path;
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, defaultJsonHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("Réponse inattendue du module d'authentification");
    }

    private HttpHeaders defaultJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

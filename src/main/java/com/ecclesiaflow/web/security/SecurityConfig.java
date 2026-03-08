package com.ecclesiaflow.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for Members module with Keycloak OAuth2 authentication.
 * <p>
 * This configuration defines endpoint access rules and configures
 * Keycloak JWT validation via OAuth2 Resource Server.
 * </p>
 * 
 * <p><strong>Architectural role:</strong> Configuration - Spring Security</p>
 * 
 * <p><strong>Main responsibilities:</strong></p>
 * <ul>
 *   <li>Configure OAuth2 Resource Server with Keycloak JWT</li>
 *   <li>Define authorization rules per endpoint</li>
 *   <li>Integrate KeycloakJwtConverter for role extraction</li>
 *   <li>Disable CSRF for stateless REST API</li>
 * </ul>
 * 
 * <p><strong>Public endpoints:</strong></p>
 * <ul>
 *   <li>/ecclesiaflow/members - New member registration</li>
 *   <li>/ecclesiaflow/members/confirmation - Email confirmation</li>
 *   <li>/actuator/health/** - Health checks for Kubernetes</li>
 * </ul>
 * 
 * <p><strong>Protected endpoints:</strong></p>
 * <ul>
 *   <li>All other endpoints require valid JWT authentication</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173}")
    private String allowedOrigins;

    private final KeycloakJwtConverter keycloakJwtConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with configured origins
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disable CSRF for stateless REST API
            .csrf(csrf -> csrf.disable())
            
            // Configure OAuth2 Resource Server with Keycloak JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter))
                .authenticationEntryPoint((request, response, authException) -> {
                    // Handle authentication errors (expired, invalid, or missing token)
                    response.setContentType("application/json");
                    response.setStatus(401);
                    response.getWriter().write(String.format(
                        "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                        java.time.Instant.now().toString(),
                        authException.getMessage(),
                        request.getRequestURI()
                    ));
                })
            )
            
            // Define authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/ecclesiaflow/members",
                    "/ecclesiaflow/members/confirmation",
                    "/ecclesiaflow/members/new-confirmation"
                ).permitAll()
                
                // Health checks for Kubernetes
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/health/liveness",
                    "/actuator/health/readiness"
                ).permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );
        
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

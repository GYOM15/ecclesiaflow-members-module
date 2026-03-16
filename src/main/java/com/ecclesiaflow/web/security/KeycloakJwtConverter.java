package com.ecclesiaflow.web.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convertisseur JWT Keycloak pour extraire les rôles et créer un token d'authentification Spring Security.
 * <p>
 * Cette classe gère l'extraction des rôles depuis différentes sources dans le JWT Keycloak :
 * <ul>
 *   <li>Claim direct "roles" (custom mapper Keycloak)</li>
 *   <li>Claim "realm_access.roles" (rôles au niveau du realm)</li>
 *   <li>Claim "resource_access.{client}.roles" (rôles au niveau des clients)</li>
 * </ul>
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Convertisseur JWT - Adaptation Keycloak/Spring Security</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Extraction des rôles depuis les différents claims JWT</li>
 *   <li>Ajout automatique du préfixe "ROLE_" pour Spring Security</li>
 *   <li>Création du JwtAuthenticationToken avec les authorities</li>
 *   <li>Extraction du principal (email ou sub)</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0
 */
@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLES_CLAIM = "roles";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, extractPrincipalName(jwt));
    }

    /**
     * Extrait toutes les authorities depuis les différentes sources du JWT.
     * Combine les rôles directs, realm_access et resource_access.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return Stream.of(
                extractDirectRoles(jwt).stream(),
                extractRealmAccessRoles(jwt).stream(),
                extractResourceAccessRoles(jwt).stream()
        )
        .flatMap(s -> s)
        .distinct()
        .collect(Collectors.toList());
    }

    /**
     * Extrait les rôles depuis le claim direct "roles".
     * Utilisé si un custom mapper Keycloak ajoute les rôles directement.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractDirectRoles(Jwt jwt) {
        Object rolesObj = jwt.getClaim(ROLES_CLAIM);
        if (rolesObj instanceof List) {
            return ((List<String>) rolesObj).stream()
                    .map(role -> new SimpleGrantedAuthority(prefixRole(role)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Extrait les rôles depuis "realm_access.roles".
     * Ce sont les rôles définis au niveau du realm Keycloak.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmAccessRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess == null) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get("roles");
        if (rolesObj instanceof List) {
            return ((List<String>) rolesObj).stream()
                    .map(role -> new SimpleGrantedAuthority(prefixRole(role)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Extrait les rôles depuis "resource_access.{client}.roles".
     * Ce sont les rôles définis au niveau des clients Keycloak.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceAccessRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);
        if (resourceAccess == null) {
            return Collections.emptyList();
        }

        return resourceAccess.values().stream()
                .filter(Map.class::isInstance)
                .map(client -> (Map<String, Object>) client)
                .filter(client -> client.containsKey("roles"))
                .flatMap(client -> {
                    Object rolesObj = client.get("roles");
                    if (rolesObj instanceof List) {
                        return ((List<String>) rolesObj).stream();
                    }
                    return Stream.empty();
                })
                .map(role -> new SimpleGrantedAuthority(prefixRole(role)))
                .collect(Collectors.toList());
    }

    /**
     * Ajoute le préfixe "ROLE_" si absent.
     * Spring Security requiert ce préfixe pour les rôles.
     */
    private String prefixRole(String role) {
        return role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
    }

    /**
     * Extrait le nom du principal depuis le JWT.
     * Utilise l'email si disponible, sinon le subject (sub).
     */
    private String extractPrincipalName(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return (email != null && !email.isBlank()) ? email : jwt.getSubject();
    }
}

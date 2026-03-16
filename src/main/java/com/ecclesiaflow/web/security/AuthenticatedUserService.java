package com.ecclesiaflow.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts authenticated user information from the Keycloak JWT
 * in the Spring Security context.
 */
@Service
@RequiredArgsConstructor
public class AuthenticatedUserService {

    /** Returns the Keycloak user ID ({@code sub} claim). */
    public String getKeycloakUserId() {
        return getJwt()
                .map(Jwt::getSubject)
                .orElseThrow(() -> new UnauthorizedException("No valid JWT found"));
    }

    /** Returns the Keycloak user ID if present. */
    public Optional<String> getKeycloakUserIdOptional() {
        return getJwt().map(Jwt::getSubject);
    }

    /** Returns the {@code email} claim from the JWT. */
    public Optional<String> getEmail() {
        return getJwt().map(jwt -> jwt.getClaimAsString("email"));
    }

    /** Returns the user's roles (without the {@code ROLE_} prefix). */
    public Set<String> getRoles() {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    /** Checks whether the user holds a specific role. */
    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    /** Parses the space-separated {@code scope} claim into a set. */
    public Set<String> getScopes() {
        return getJwt()
                .map(jwt -> jwt.getClaimAsString("scope"))
                .map(scopeString -> Set.of(scopeString.split(" ")))
                .orElse(Set.of());
    }

    /** Returns the {@code identity_provider} claim from the JWT (e.g. "google", "facebook", "microsoft"). */
    public Optional<String> getIdentityProvider() {
        return getJwt().map(jwt -> jwt.getClaimAsString("identity_provider"));
    }

    /** Returns whether the {@code email_verified} claim is true. */
    public boolean isEmailVerified() {
        return getJwt()
                .map(jwt -> jwt.getClaimAsBoolean("email_verified"))
                .orElse(false);
    }

    /** Returns the JWT from the current security context. */
    public Optional<Jwt> getJwt() {
        return getAuthentication()
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken());
    }

    private Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated);
    }
}

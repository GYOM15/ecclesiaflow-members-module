package com.ecclesiaflow.web.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KeycloakJwtConverter - JWT to Spring Security token conversion")
class KeycloakJwtConverterTest {

    private final KeycloakJwtConverter converter = new KeycloakJwtConverter();

    private Jwt buildJwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-uuid-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        claims.forEach(builder::claim);
        return builder.build();
    }

    @Nested
    @DisplayName("Role extraction")
    class RoleExtraction {

        @Test
        @DisplayName("should extract direct roles claim")
        void shouldExtractDirectRoles() {
            Jwt jwt = buildJwt(Map.of("roles", List.of("USER", "ADMIN")));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(authorityStrings(token)).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("should extract realm_access roles")
        void shouldExtractRealmAccessRoles() {
            Jwt jwt = buildJwt(Map.of(
                    "realm_access", Map.of("roles", List.of("offline_access", "uma_authorization"))
            ));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(authorityStrings(token))
                    .containsExactlyInAnyOrder("ROLE_offline_access", "ROLE_uma_authorization");
        }

        @Test
        @DisplayName("should extract resource_access roles from multiple clients")
        void shouldExtractResourceAccessRoles() {
            Jwt jwt = buildJwt(Map.of(
                    "resource_access", Map.of(
                            "ecclesiaflow-frontend", Map.of("roles", List.of("member")),
                            "account", Map.of("roles", List.of("manage-account"))
                    )
            ));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(authorityStrings(token))
                    .containsExactlyInAnyOrder("ROLE_member", "ROLE_manage-account");
        }

        @Test
        @DisplayName("should combine roles from all sources without duplicates")
        void shouldCombineAllSourcesNoDuplicates() {
            Jwt jwt = buildJwt(Map.of(
                    "roles", List.of("USER"),
                    "realm_access", Map.of("roles", List.of("USER", "offline_access")),
                    "resource_access", Map.of("client", Map.of("roles", List.of("member")))
            ));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(authorityStrings(token))
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_offline_access", "ROLE_member");
        }

        @Test
        @DisplayName("should handle missing realm_access gracefully")
        void shouldHandleMissingRealmAccess() {
            Jwt jwt = buildJwt(Map.of());

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(token.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("should handle null resource_access gracefully")
        void shouldHandleNullResourceAccess() {
            Jwt jwt = buildJwt(Map.of(
                    "realm_access", Map.of("roles", List.of("USER"))
            ));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(authorityStrings(token)).containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("should not double-prefix roles that already have ROLE_")
        void shouldNotDoublePrefixRoles() {
            Jwt jwt = buildJwt(Map.of("roles", List.of("ROLE_ADMIN")));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(authorityStrings(token)).containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should handle realm_access with no roles key")
        void shouldHandleRealmAccessWithoutRolesKey() {
            Jwt jwt = buildJwt(Map.of(
                    "realm_access", Map.of("other_key", "value")
            ));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(token.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("should handle resource_access with non-map entries")
        void shouldHandleResourceAccessNonMapEntries() {
            Jwt jwt = buildJwt(Map.of(
                    "resource_access", Map.of(
                            "valid-client", Map.of("roles", List.of("admin")),
                            "invalid-entry", "not-a-map"
                    )
            ));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(authorityStrings(token)).containsExactly("ROLE_admin");
        }
    }

    @Nested
    @DisplayName("Principal name extraction")
    class PrincipalName {

        @Test
        @DisplayName("should use email as principal when available")
        void shouldUseEmailAsPrincipal() {
            Jwt jwt = buildJwt(Map.of("email", "user@example.com"));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(token.getName()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should fall back to subject when email is absent")
        void shouldFallbackToSubject() {
            Jwt jwt = buildJwt(Map.of());

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(token.getName()).isEqualTo("user-uuid-123");
        }

        @Test
        @DisplayName("should fall back to subject when email is blank")
        void shouldFallbackToSubjectWhenEmailBlank() {
            Jwt jwt = buildJwt(Map.of("email", "  "));

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(token.getName()).isEqualTo("user-uuid-123");
        }
    }

    private Collection<String> authorityStrings(AbstractAuthenticationToken token) {
        return token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}

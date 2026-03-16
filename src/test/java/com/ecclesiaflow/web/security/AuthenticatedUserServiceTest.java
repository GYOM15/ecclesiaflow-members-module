package com.ecclesiaflow.web.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuthenticatedUserService - JWT claim extraction")
class AuthenticatedUserServiceTest {

    private final AuthenticatedUserService service = new AuthenticatedUserService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        claims.forEach(builder::claim);
        return builder.build();
    }

    private void setAuthentication(Jwt jwt, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Nested
    @DisplayName("getKeycloakUserId")
    class GetKeycloakUserId {

        @Test
        @DisplayName("should return subject from JWT")
        void shouldReturnSubject() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt);

            assertThat(service.getKeycloakUserId()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when no authentication")
        void shouldThrowWhenNoAuth() {
            assertThatThrownBy(() -> service.getKeycloakUserId())
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    @DisplayName("getKeycloakUserIdOptional")
    class GetKeycloakUserIdOptional {

        @Test
        @DisplayName("should return present when authenticated")
        void shouldReturnPresent() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt);

            assertThat(service.getKeycloakUserIdOptional()).isPresent().contains("user-123");
        }

        @Test
        @DisplayName("should return empty when no authentication")
        void shouldReturnEmpty() {
            assertThat(service.getKeycloakUserIdOptional()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getEmail")
    class GetEmail {

        @Test
        @DisplayName("should return email claim")
        void shouldReturnEmail() {
            Jwt jwt = buildJwt(Map.of("email", "test@example.com"));
            setAuthentication(jwt);

            assertThat(service.getEmail()).isPresent().contains("test@example.com");
        }

        @Test
        @DisplayName("should return empty when no email claim")
        void shouldReturnEmptyWhenNoClaim() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt);

            assertThat(service.getEmail()).isEmpty();
        }

        @Test
        @DisplayName("should return empty when no authentication")
        void shouldReturnEmptyWhenNoAuth() {
            assertThat(service.getEmail()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRoles")
    class GetRoles {

        @Test
        @DisplayName("should return roles without ROLE_ prefix")
        void shouldReturnRoles() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt, "USER", "ADMIN");

            Set<String> roles = service.getRoles();
            assertThat(roles).containsExactlyInAnyOrder("USER", "ADMIN");
        }

        @Test
        @DisplayName("should return empty set when no authentication")
        void shouldReturnEmptyWhenNoAuth() {
            assertThat(service.getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasRole")
    class HasRole {

        @Test
        @DisplayName("should return true for existing role")
        void shouldReturnTrue() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt, "ADMIN");

            assertThat(service.hasRole("ADMIN")).isTrue();
        }

        @Test
        @DisplayName("should return false for missing role")
        void shouldReturnFalse() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt, "USER");

            assertThat(service.hasRole("ADMIN")).isFalse();
        }
    }

    @Nested
    @DisplayName("getScopes")
    class GetScopes {

        @Test
        @DisplayName("should parse space-separated scope claim")
        void shouldParseScopes() {
            Jwt jwt = buildJwt(Map.of("scope", "openid profile email"));
            setAuthentication(jwt);

            assertThat(service.getScopes()).containsExactlyInAnyOrder("openid", "profile", "email");
        }

        @Test
        @DisplayName("should return empty set when no scope claim")
        void shouldReturnEmptyWhenNoScope() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt);

            assertThat(service.getScopes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getIdentityProvider")
    class GetIdentityProvider {

        @Test
        @DisplayName("should return identity_provider claim")
        void shouldReturnIdp() {
            Jwt jwt = buildJwt(Map.of("identity_provider", "google"));
            setAuthentication(jwt);

            assertThat(service.getIdentityProvider()).isPresent().contains("google");
        }

        @Test
        @DisplayName("should return empty when no identity_provider claim")
        void shouldReturnEmpty() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt);

            assertThat(service.getIdentityProvider()).isEmpty();
        }
    }

    @Nested
    @DisplayName("isEmailVerified")
    class IsEmailVerified {

        @Test
        @DisplayName("should return true when email_verified is true")
        void shouldReturnTrue() {
            Jwt jwt = buildJwt(Map.of("email_verified", true));
            setAuthentication(jwt);

            assertThat(service.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("should return false when email_verified is false")
        void shouldReturnFalse() {
            Jwt jwt = buildJwt(Map.of("email_verified", false));
            setAuthentication(jwt);

            assertThat(service.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("should return false when no authentication")
        void shouldReturnFalseWhenNoAuth() {
            assertThat(service.isEmailVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("getJwt")
    class GetJwt {

        @Test
        @DisplayName("should return JWT when authenticated")
        void shouldReturnJwt() {
            Jwt jwt = buildJwt(Map.of());
            setAuthentication(jwt);

            Optional<Jwt> result = service.getJwt();
            assertThat(result).isPresent();
            assertThat(result.get().getSubject()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("should return empty when no authentication")
        void shouldReturnEmpty() {
            assertThat(service.getJwt()).isEmpty();
        }
    }
}

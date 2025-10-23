package com.ecclesiaflow.business.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour AuthenticatedUserContextProvider.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticatedUserContextProviderTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthenticatedUserContextProvider contextProvider;

    private static final String JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private String validToken;
    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(contextProvider, "jwtSecret", JWT_SECRET);
        testMemberId = UUID.randomUUID();
        validToken = generateTestToken(testMemberId, "test@example.com", "ef:members:read:own ef:members:write:own");
    }

    @Test
    void getAuthenticatedMemberId_shouldExtractMemberIdFromJWT() {
        // Given
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // When
        UUID result = contextProvider.getAuthenticatedMemberId();

        // Then
        assertThat(result).isEqualTo(testMemberId);
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedUserScopes_shouldExtractScopesFromJWT() {
        // Given
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // When
        List<String> result = contextProvider.getAuthenticatedUserScopes();

        // Then
        assertThat(result).containsExactlyInAnyOrder("ef:members:read:own", "ef:members:write:own");
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedMemberId_shouldThrowException_whenNoAuthorizationHeader() {
        // Given
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> contextProvider.getAuthenticatedMemberId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing or invalid Authorization header");
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedMemberId_shouldThrowException_whenInvalidToken() {
        // Given
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        // When & Then
        assertThatThrownBy(() -> contextProvider.getAuthenticatedMemberId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid JWT token");
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedMemberId_shouldThrowException_whenMissingCidClaim() {
        // Given
        String tokenWithoutCid = generateTokenWithoutCid("test@example.com");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithoutCid);

        // When & Then
        assertThatThrownBy(() -> contextProvider.getAuthenticatedMemberId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing 'cid' claim");
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedUserScopes_shouldReturnEmptyList_whenScopeClaimIsEmpty() {
        // Given
        String tokenWithEmptyScopes = generateTestToken(testMemberId, "test@example.com", "");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithEmptyScopes);

        // When
        List<String> result = contextProvider.getAuthenticatedUserScopes();

        // Then
        assertThat(result).isEmpty();
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedUserScopes_shouldReturnEmptyList_whenScopeClaimIsMissing() {
        // Given
        String tokenWithoutScopes = generateTokenWithoutCid("test@example.com");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithoutScopes);

        // When
        List<String> result = contextProvider.getAuthenticatedUserScopes();

        // Then
        assertThat(result).isEmpty();
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedMemberId_shouldThrowException_whenNoRequestContext() {
        // Given
        RequestContextHolder.resetRequestAttributes();

        // When & Then
        assertThatThrownBy(() -> contextProvider.getAuthenticatedMemberId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No HTTP request context available");
    }

    @Test
    void getAuthenticatedMemberId_shouldThrowException_whenAuthorizationHeaderDoesNotStartWithBearer() {
        // Given
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Basic invalid");

        // When & Then
        assertThatThrownBy(() -> contextProvider.getAuthenticatedMemberId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing or invalid Authorization header");
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedMemberId_shouldThrowException_whenCidClaimIsBlank() {
        // Given
        String tokenWithBlankCid = generateTokenWithBlankCid("test@example.com");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithBlankCid);

        // When & Then
        assertThatThrownBy(() -> contextProvider.getAuthenticatedMemberId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing 'cid' claim");
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthenticatedMemberId_shouldThrowException_whenCidIsNotValidUUID() {
        // Given
        String tokenWithInvalidCid = generateTokenWithInvalidCid("test@example.com");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithInvalidCid);

        // When & Then
        assertThatThrownBy(() -> contextProvider.getAuthenticatedMemberId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("'cid' is not a valid UUID");
        
        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    // Helper methods
    private String generateTestToken(UUID memberId, String email, String scopes) {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .claim("cid", memberId.toString())
                .claim("scope", scopes)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    private String generateTokenWithoutCid(String email) {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    private String generateTokenWithBlankCid(String email) {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .claim("cid", "   ")  // Blank string
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    private String generateTokenWithInvalidCid(String email) {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .claim("cid", "not-a-valid-uuid")  // Invalid UUID format
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }
}

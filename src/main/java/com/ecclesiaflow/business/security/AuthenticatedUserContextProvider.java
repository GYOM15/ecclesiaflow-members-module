package com.ecclesiaflow.business.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Fournisseur de contexte pour l'utilisateur authentifié.
 * <p>
 * Extrait les informations de l'utilisateur authentifié depuis le token JWT
 * présent dans le header Authorization de la requête HTTP.
 * </p>
 * 
 * <p><strong>⚠️ IMPLÉMENTATION TEMPORAIRE :</strong></p>
 * <p>
 * Cette classe parse et valide le JWT directement dans le microservice.
 * <strong>Avec l'introduction d'Envoy Proxy, cette logique sera déplacée :</strong>
 * </p>
 * <ul>
 *   <li><strong>Envoy</strong> validera le JWT en amont (External Authorization Filter)</li>
 *   <li><strong>Envoy</strong> extraira les claims et les injectera dans des headers HTTP enrichis :
 *     <ul>
 *       <li>{@code X-User-Id: 550e8400-e29b-41d4-a716-446655440000}</li>
 *       <li>{@code X-User-Scopes: ef:members:read:own ef:members:write:own}</li>
 *     </ul>
 *   </li>
 *   <li><strong>Ce microservice</strong> lira simplement les headers (pas de parsing JWT)</li>
 * </ul>
 * <p>
 * <strong>Migration future :</strong> Cette classe sera remplacée par un simple extracteur de headers.
 * Les dépendances JJWT pourront être retirées du microservice.
 * </p>
 * 
 * <p><strong>Informations extraites :</strong></p>
 * <ul>
 *   <li><strong>memberId :</strong> Identifiant unique du membre (claim "sub")</li>
 *   <li><strong>scopes :</strong> Liste des permissions (claim "scope" séparé par espaces)</li>
 * </ul>
 * 
 * <p><strong>Format du token JWT :</strong></p>
 * <pre>
 * Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * 
 * Payload (claims):
 * {
 *   "sub": "550e8400-e29b-41d4-a716-446655440000",  // memberId
 *   "scope": "ef:members:read:own ef:members:write:own",
 *   "iat": 1234567890,
 *   "exp": 1234571490
 * }
 * </pre>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see ScopeValidationAspect
 * @see RequireScopes
 * @deprecated Cette implémentation sera remplacée par un extracteur de headers Envoy
 */
@Deprecated
@Component
public class AuthenticatedUserContextProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Extrait l'identifiant du membre authentifié depuis le JWT.
     * <p>
     * Le memberId est extrait du claim 'cid' (custom ID) du JWT.
     * Le claim 'sub' contient l'email, pas le memberId.
     * </p>
     * 
     * @return UUID du membre authentifié
     * @throws IllegalStateException si le token est absent ou invalide
     */
    public UUID getAuthenticatedMemberId() {
        Claims claims = extractClaims();
        String memberIdStr = claims.get("cid", String.class);
        
        if (memberIdStr == null || memberIdStr.isBlank()) {
            throw new IllegalStateException("Invalid JWT: missing 'cid' claim");
        }
        
        try {
            return UUID.fromString(memberIdStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid JWT: 'cid' is not a valid UUID", e);
        }
    }

    /**
     * Extrait les scopes (permissions) du membre authentifié depuis le JWT.
     * 
     * @return liste des scopes (ex: ["ef:members:read:own", "ef:members:write:own"])
     * @throws IllegalStateException si le token est absent ou invalide
     */
    public List<String> getAuthenticatedUserScopes() {
        Claims claims = extractClaims();
        String scopesClaim = claims.get("scope", String.class);
        
        if (scopesClaim == null || scopesClaim.isBlank()) {
            return Collections.emptyList();
        }
        
        // Les scopes sont séparés par des espaces dans le claim "scope"
        return Arrays.asList(scopesClaim.split("\\s+"));
    }

    /**
     * Extrait les claims du JWT depuis le header Authorization.
     * 
     * @return claims du JWT
     * @throws IllegalStateException si le token est absent ou invalide
     */
    private Claims extractClaims() {
        String token = extractTokenFromRequest();
        
        try {
            // Décoder le secret depuis base64 (même méthode que le module auth)
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT token", e);
        }
    }

    /**
     * Extrait le token JWT depuis le header Authorization de la requête HTTP.
     * 
     * @return token JWT (sans le préfixe "Bearer ")
     * @throws IllegalStateException si le header est absent ou mal formaté
     */
    private String extractTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            throw new IllegalStateException("No HTTP request context available");
        }
        
        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Missing or invalid Authorization header");
        }
        
        return authHeader.substring(7); // Retire "Bearer "
    }
}

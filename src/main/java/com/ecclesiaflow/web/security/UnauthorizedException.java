package com.ecclesiaflow.web.security;

/**
 * Exception levée lorsqu'un utilisateur tente d'accéder à une ressource sans authentification valide.
 * <p>
 * Cette exception est utilisée lorsque le JWT Keycloak est absent ou invalide
 * dans le SecurityContext Spring Security.
 * </p>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

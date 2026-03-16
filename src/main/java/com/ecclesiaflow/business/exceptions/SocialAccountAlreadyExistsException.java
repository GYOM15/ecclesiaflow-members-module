package com.ecclesiaflow.business.exceptions;

/**
 * Thrown when a social onboarding attempt conflicts with an existing member
 * (duplicate email or keycloakUserId). Maps to HTTP 409 Conflict.
 */
public class SocialAccountAlreadyExistsException extends RuntimeException {

    public SocialAccountAlreadyExistsException(String message) {
        super(message);
    }
}

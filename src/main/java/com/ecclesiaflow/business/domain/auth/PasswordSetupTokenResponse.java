package com.ecclesiaflow.business.domain.auth;

/**
 * Response containing the password setup token and endpoint.
 *
 * @param token           the opaque setup token
 * @param expiresInSeconds token validity in seconds
 * @param passwordEndpoint URL endpoint for password setup
 */
public record PasswordSetupTokenResponse(
        String token,
        int expiresInSeconds,
        String passwordEndpoint
) {}

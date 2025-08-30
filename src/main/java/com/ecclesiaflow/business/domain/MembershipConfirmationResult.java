package com.ecclesiaflow.business.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Objet métier représentant le résultat d'une confirmation de membre
 * (sans DTO de réponse - respecte la séparation des couches)
 */
@Data
@Builder
public class MembershipConfirmationResult {
    private String message;
    private String temporaryToken;
    private long expiresInSeconds;
}

package com.ecclesiaflow.business.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Objet métier représentant une demande de confirmation de membre
 * (sans DTO - respecte la séparation des couches)
 */
@Data
@Builder
public class MembershipConfirmation {
    private UUID memberId;
    private String confirmationCode;
}

package com.ecclesiaflow.business.mappers.web;

import com.ecclesiaflow.business.domain.MembershipConfirmationResult;
import com.ecclesiaflow.web.dto.ConfirmationResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper pour transformer les objets métier de confirmation en DTOs de réponse
 * Respecte la séparation des couches : services métier → mapper → DTOs web
 */
@Component
public class ConfirmationResponseMapper {

    /**
     * Transforme un résultat de confirmation métier en DTO de réponse
     */
    public ConfirmationResponse fromMemberConfirmationResult(MembershipConfirmationResult result) {
        return ConfirmationResponse.builder()
                .message(result.getMessage())
                .temporaryToken(result.getTemporaryToken())
                .expiresIn(result.getExpiresInSeconds())
                .build();
    }
}

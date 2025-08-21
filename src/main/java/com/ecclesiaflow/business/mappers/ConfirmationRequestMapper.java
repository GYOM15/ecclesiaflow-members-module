package com.ecclesiaflow.business.mappers;

import com.ecclesiaflow.business.domain.MembershipConfirmation;
import com.ecclesiaflow.web.dto.ConfirmationRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper pour transformer les DTOs de confirmation en objets métier
 * Respecte la séparation des couches : DTOs web → mapper → objets métier
 */
@Component
public class ConfirmationRequestMapper {

    /**
     * Transforme un DTO de confirmation et un memberId en objet métier
     */
    public MembershipConfirmation fromConfirmationRequest(UUID memberId, ConfirmationRequest request) {
        if (request == null) {
            return MembershipConfirmation.builder()
                    .memberId(memberId)
                    .confirmationCode("")
                    .build();
        }

        
        return MembershipConfirmation.builder()
                .memberId(memberId)
                .confirmationCode(request.getCode())
                .build();
    }
}

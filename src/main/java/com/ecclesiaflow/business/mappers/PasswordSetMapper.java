package com.ecclesiaflow.business.mappers;

import com.ecclesiaflow.business.domain.MembershipPassword;
import com.ecclesiaflow.web.dto.SetPasswordRequest;
import com.ecclesiaflow.web.dto.PasswordSetResponse;
import org.springframework.stereotype.Component;


@Component
public class PasswordSetMapper {
    public MembershipPassword fromSetPasswordRequest(SetPasswordRequest request) {
        return MembershipPassword.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }

    /**
     * Crée une réponse de succès pour la définition de mot de passe
     */
    public PasswordSetResponse toSuccessResponse() {
        return PasswordSetResponse.builder()
                .message("Mot de passe défini avec succès")
                .status("SUCCESS")
                .build();
    }
}

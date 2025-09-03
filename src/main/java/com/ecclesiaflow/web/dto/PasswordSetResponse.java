package com.ecclesiaflow.web.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de réponse pour la définition de mot de passe
 */
@Data
@Builder
public class PasswordSetResponse {
    private String message;
    private String status;
}

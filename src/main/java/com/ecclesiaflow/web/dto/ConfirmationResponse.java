package com.ecclesiaflow.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfirmationResponse {
    private String message;
    private String temporaryToken;
    private long expiresIn; // Durée de validité en secondes
}

package com.ecclesiaflow.web.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ConfirmationRequest {
    @NotBlank(message = "Le code de confirmation est obligatoire")
    @Pattern(regexp = "\\d{6}", message = "Le code doit contenir exactement 6 chiffres")
    private String code;
}

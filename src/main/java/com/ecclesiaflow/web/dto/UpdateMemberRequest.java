package com.ecclesiaflow.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMemberRequest {
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String lastName;

    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;

    @Size(max = 200, message = "L'adresse ne peut pas dépasser 200 caractères")
    private String address;

    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;
}
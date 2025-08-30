package com.ecclesiaflow.web.controller;

import com.ecclesiaflow.business.services.impl.MemberPasswordService;
import com.ecclesiaflow.web.dto.SetPasswordRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/ecclesiaflow/members")
@RequiredArgsConstructor
@Tag(name = "Member Password", description = "Gestion des mots de passe des membres")
public class MemberPasswordController {

    private final MemberPasswordService memberPasswordService;

    @PostMapping(value = "/{memberId}/password", produces = "application/vnd.ecclesiaflow.members.v1+json")
    public ResponseEntity<Void> setPassword(
            @PathVariable UUID memberId,
            @RequestBody @Valid SetPasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = extractBearerToken(authHeader);
        // Valider que l'email correspond au memberId via le module Members
        UUID realMemberId = memberPasswordService.getMemberIdByEmail(request.getEmail());
        if (!realMemberId.equals(memberId)) {
            throw new IllegalArgumentException("L'email ne correspond pas à l'ID du membre");
        }
        memberPasswordService.setPassword(request.getEmail(), request.getPassword(), token);
        return ResponseEntity.ok().build();
    }




    /**
     * Extrait le token du header Authorization
     */
    private String extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Format du token invalide");
    }
}


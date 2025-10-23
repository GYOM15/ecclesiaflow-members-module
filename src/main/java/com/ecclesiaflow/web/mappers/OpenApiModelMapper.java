package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.confirmation.MembershipConfirmationResult;
import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.web.model.ConfirmationResponse;
import com.ecclesiaflow.web.model.MemberPageResponse;
import com.ecclesiaflow.web.model.SignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour créer les modèles OpenAPI générés à partir des entités métier.
 * <p>
 * Ce mapper crée directement les modèles OpenAPI générés à partir des entités Member,
 * en respectant la structure définie dans la spécification OpenAPI members.yaml.
 * </p>
 * 
 * <p><strong>Structure des modèles OpenAPI :</strong></p>
 * <ul>
 *   <li>SignUpResponse : structure plate avec tous les champs directement (email, firstName, etc.)</li>
 *   <li>MemberPageResponse : contient une liste de SignUpResponse dans le champ 'content'</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class OpenApiModelMapper {

    @Value("${ecclesiaflow.auth-module.base-url:http://localhost:8081}")
    private String authModuleBaseUrl;

    /**
     * Crée un SignUpResponse OpenAPI à partir d'un Member et d'un message.
     * 
     * @param member Entité membre
     * @param message Message de succès
     * @return Modèle OpenAPI SignUpResponse
     */
    public SignUpResponse createSignUpResponse(Member member, String message) {
        SignUpResponse response = new SignUpResponse();
        response.setMessage(message);
        
        if (member != null) {
            populateSignUpResponseFromMember(member, response);
        }
        
        return response;
    }

    private void populateSignUpResponseFromMember(Member member, SignUpResponse response) {
        response.setEmail(member.getEmail());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setAddress(member.getAddress());
        response.setConfirmed(member.isConfirmed());
        response.setRole(member.getRole().name());

        if (member.getCreatedAt() != null) {
            response.setCreatedAt(member.getCreatedAt().toString());
        }
        if (member.getConfirmedAt() != null) {
            response.setConfirmedAt(member.getConfirmedAt().toString());
        }
    }

    /**
     * Crée un MemberPageResponse OpenAPI à partir d'une Page de Member.
     * 
     * @param memberPage Page de membres du domaine
     * @return Modèle OpenAPI MemberPageResponse
     */
    public MemberPageResponse createMemberPageResponse(Page<Member> memberPage) {
        MemberPageResponse response = new MemberPageResponse();
        
        List<SignUpResponse> content = memberPage.getContent().stream()
            .map(member -> {
                SignUpResponse memberResponse = new SignUpResponse();
                populateSignUpResponseFromMember(member, memberResponse);

                return memberResponse;
            })
            .collect(Collectors.toList());
        
        response.setContent(content);
        response.setPage(memberPage.getNumber());
        response.setNumber(memberPage.getNumber());
        response.setSize(memberPage.getSize());
        response.setTotalElements(memberPage.getTotalElements());
        response.setTotalPages(memberPage.getTotalPages());
        response.setFirst(memberPage.isFirst());
        response.setLast(memberPage.isLast());
        response.setNumberOfElements(memberPage.getNumberOfElements());
        response.setEmpty(memberPage.isEmpty());
        
        return response;
    }

    /**
     * Crée un ConfirmationResponse OpenAPI à partir d'un MembershipConfirmationResult.
     * <p>
     * Convertit le résultat de confirmation métier vers le modèle OpenAPI.
     * </p>
     * 
     * @param result Résultat de la confirmation métier
     * @return Modèle OpenAPI ConfirmationResponse
     */
    public ConfirmationResponse createConfirmationResponse(MembershipConfirmationResult result) {
        ConfirmationResponse response = new ConfirmationResponse();
        
        if (result != null) {
            response.setMessage(result.getMessage());
            response.setTemporaryToken(result.getTemporaryToken());
            response.setExpiresIn(result.getExpiresInSeconds());
            
            try {
                String passwordUrl = authModuleBaseUrl + "/ecclesiaflow/auth/password";
                response.setPasswordEndpoint(new java.net.URI(passwordUrl));
            } catch (java.net.URISyntaxException e) {
                // Log l'erreur mais continue
            }
        }
        
        return response;
    }
}

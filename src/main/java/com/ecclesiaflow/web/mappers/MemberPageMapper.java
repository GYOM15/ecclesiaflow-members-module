package com.ecclesiaflow.web.mappers;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.web.dto.MemberPageResponse;
import com.ecclesiaflow.web.dto.SignUpResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper pour la conversion des pages de membres entre la couche domaine et la couche web.
 * 
 * <p>Cette classe convertit les objets {@link Page<Member>} du domaine vers les DTOs
 * {@link MemberPageResponse} pour la couche web, en préservant toutes les métadonnées
 * de pagination nécessaires pour l'interface utilisateur.</p>
 * 
 * <p><strong>Responsabilités :</strong></p>
 * <ul>
 *   <li>Conversion des pages de membres domaine vers DTOs web</li>
 *   <li>Préservation des métadonnées de pagination</li>
 *   <li>Délégation de la conversion des membres individuels</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
public class MemberPageMapper {
    
    /**
     * Convertit une page de membres domaine en DTO de réponse paginée.
     * 
     * @param memberPage la page de membres du domaine, non null
     * @return le DTO de réponse paginée correspondant
     * @throws IllegalArgumentException si memberPage est null
     */
    public MemberPageResponse toPageResponse(Page<Member> memberPage) {
        if (memberPage == null) {
            throw new IllegalArgumentException("Member page cannot be null");
        }
        
        // Convertir les membres individuels en utilisant le mapper statique
        List<SignUpResponse> memberResponses = memberPage.getContent()
                .stream()
                .map(member -> MemberResponseMapper.fromMember(member, "Membre récupéré"))
                .toList();

        return MemberPageResponse.builder()
                .content(memberResponses)
                .page(memberPage.getNumber())
                .number(memberPage.getNumber())
                .size(memberPage.getSize())
                .totalElements(memberPage.getTotalElements())
                .totalPages(memberPage.getTotalPages())
                .first(memberPage.isFirst())
                .last(memberPage.isLast())
                .numberOfElements(memberPage.getNumberOfElements())
                .empty(memberPage.isEmpty())
                .build();
    }
}

package com.ecclesiaflow.business.mappers;

import com.ecclesiaflow.business.domain.MembershipUpdate;
import com.ecclesiaflow.web.dto.UpdateMemberRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper pour transformer les DTOs de modification de membre en objets métier.
 * <p>
 * Respecte la séparation des couches : DTOs web → mapper → objets métier → services.
 * Les services métier ne doivent jamais manipuler directement les DTOs de la couche web.
 * </p>
 * 
 * <p><strong>Pattern de mapping :</strong></p>
 * <ul>
 *   <li>Entrée : DTO web + données contextuelles</li>
 *   <li>Sortie : Objet métier pur</li>
 *   <li>Validation : Gestion des cas null et données manquantes</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Component
public class MemberUpdateMapper {

    /**
     * Transforme un DTO de modification et un memberId en objet métier.
     * 
     * @param memberId ID du membre à modifier
     * @param updateRequest DTO contenant les nouvelles données
     * @return Objet métier pour la couche business
     */
    public MembershipUpdate fromUpdateMemberRequest(UUID memberId, UpdateMemberRequest updateRequest) {
        if (updateRequest == null) {
            return MembershipUpdate.builder()
                    .memberId(memberId)
                    .build();
        }

        return MembershipUpdate.builder()
                .memberId(memberId)
                .firstName(updateRequest.getFirstName())
                .lastName(updateRequest.getLastName())
                .address(updateRequest.getAddress())
                .email(updateRequest.getEmail())
                .build();
    }
}

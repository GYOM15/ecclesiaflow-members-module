package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.Member;
import com.ecclesiaflow.io.persistence.entities.MemberEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberPersistenceMapper {

    /**
     * Convertit une entité de persistance MemberEntity en un objet du domaine Member.
     *
     * @param entity l'entité de persistance
     * @return l'objet du domaine
     */
    public Member toDomain(MemberEntity entity) {
        return Member.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .role(entity.getRole())
                .confirmed(entity.isConfirmed())
                .passwordSet(entity.isPasswordSet())
                .createdAt(entity.getCreatedAt())
                .confirmedAt(entity.getConfirmedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convertit un objet du domaine Member en une entité de persistance MemberEntity.
     * Cette méthode ne doit pas mapper les champs gérés automatiquement par la base de données
     * lors de la création initiale.
     *
     * @param domain l'objet du domaine
     * @return l'entité de persistance
     */
    public MemberEntity toEntity(Member domain) {
        return MemberEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .address(domain.getAddress())
                .role(domain.getRole())
                .confirmed(domain.isConfirmed())
                .passwordSet(domain.isPasswordSet())
                .createdAt(domain.getCreatedAt())
                .confirmedAt(domain.getConfirmedAt())
                .build();
    }
}
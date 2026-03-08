package com.ecclesiaflow.io.persistence.mappers;

import com.ecclesiaflow.business.domain.confirmation.MemberConfirmation;
import com.ecclesiaflow.io.persistence.jpa.MemberConfirmationEntity;
import org.springframework.stereotype.Component;

/** Converts between MemberConfirmation domain objects and MemberConfirmationEntity JPA entities. */
@Component
public class MemberConfirmationPersistenceMapper {

    /** Converts a JPA entity to a domain object. */
    public MemberConfirmation toDomain(MemberConfirmationEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }

        return MemberConfirmation.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .token(entity.getToken())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    /** Converts a domain object to a JPA entity. */
    public MemberConfirmationEntity toEntity(MemberConfirmation domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Domain object must not be null");
        }

        return MemberConfirmationEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .token(domain.getToken())
                .createdAt(domain.getCreatedAt())
                .expiresAt(domain.getExpiresAt())
                .build();
    }
}
